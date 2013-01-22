# -*- coding: utf-8 -*-
"""
Simple export library for pcraster/python delwaq link. The module can be
used to export an ldd to a delwaq pointer file and fill the input arrays.

.. warning::
    
    This is an experimental version. A complete redesign is needed as this 
    version is unstable and very poorly sstructured!



the wflow run should have saved at least the folowing mapstacks::

    - self.OldKinWaveVolume=vol
    - self.SurfaceRunoff=run
    - self.Inwater=inw  (or the different components that make up this flux)
    
Command line options::
    
    -C: caseDir - set the wflow case directory to use
    -R: runId - set the wflow runId to use
    -T: Set last timestep
    -O: set starttime ('%Y-%m-%d% H:%M:%S')
    -d: if this option set the dynamic date (timeseries) are also converted 
        and written
    -A: sets the areamap used to specify the fraction sources. This can be
        a subcatcment map, a soil type map, a land use maps etc. Default is:
        staticmaps/wflow_subcatch.map (relative to the caseDir directory)
    -D: delwaqdir - set the basedir to create the delwaq schematisation in 
    -S: sourcemap - name of the wflow output map to use as source. 
        it should be a variable that flows into the kinematic wave routine
        inw is normally used as it contain all water per cell that flows into
        the kinematic wave function.
        Use multiple -S options to include multiple maps
    -s: Set the model timesteps in seconds (default 86400)
    -F: if set wflow is expected to be run by FEWS. It will determine
        the timesteps from the runinfo.xml file and save the output initial
        conditions to an alternate location. The runinfo.xml file should be located
        in the inmaps directory of the case. 
        
    ..todo::
        
        test the multiple sources option
        
    ..todo::
        
        Add option to seperate construction of network from filling of the input
        arrays
        
    ..todo::
        
        Add state handling
        
$Author: schelle $
$Id: wflow_hbv.py 331 2012-06-13 12:07:52Z schelle $
$Rev: 331 $        
"""

# 1: maak een kaart met unieke id's van het dem
# 2: van elke cel de dosnstream

from wf_DynamicFramework import *
from datetime import *
import wflow_adapt
import os
import os.path
import shutil, glob
import getopt


logger = ""
volumeMapStack="vol"
runoffMapStack="run"

def dw_WriteNrSegments(fname,nr):
    """ 
    Writes the number segments to file 
    
    B3\_nrofseg.inc
    """
    exfile = open(fname,'w')
    print >>exfile,";Written by dw_WriteNrSegments"
    print >>exfile,str(nr) + " ; nr of segments"
    exfile.close()
    
def dw_WriteNrExChnages(fname,nr):
    """ 
    Writes the number of exchnages to file (number of rows in the pointer file)
    
    B4\_nrofexch.inc
    """
    exfile = open(fname,'w')
    print >>exfile,";Written by dw_WriteNrExChnages"
    print >>exfile,str(nr) + " 0 0 ; x, y, z direction"
    exfile.close()


def dw_WriteBoundData(fname,areas):
    """ 
    writes B5\_bounddata.inc
    """  
    
    areas = sorted(areas,reverse=True)
    exfile = open(fname,'w')
    print >>exfile,";Written by dw_WriteBoundData"
    for i in areas:
        print >>exfile, "ITEM \'Area_%s\'" %  (i)
        print >>exfile, "CONCENTRATION  \'Area_%s\' \'Check\'" %  (i)
        print >>exfile, "DATA"
        print >>exfile, "1.0  1.0"
        print >>exfile, ""
 
    exfile.close()

    
def dw_WriteInitials(fname,inmaps):
    """
    B8_initials.inc
    """
    
    maps = ['Initial','Check']
    exfile = open(fname,'w')
    print >>exfile,"INITIALS"
    for rr in inmaps:
        print >>exfile,"'" + rr + "'", 
    for rr in maps:
        print >>exfile,"'" + rr + "'",        
    print >>exfile
    print >>exfile,"DEFAULTS"
    for rr in inmaps:
        print >>exfile,str(0.0) + " ",
    for rr in maps:
        print >>exfile,str(1.0) + " ",
    print >>exfile    
    exfile.close()
    
    
def dw_WriteBoundlist(fname,pointer,areas,of,inflowtypes):
    """ 
    Writes the boundary list file
    B5\_boundlist.inc
    Numbering is abs(exchnage id)
    
    Input: 
        - fname, pointer
        
    TDOD: 
        - add labeling of different inflows
    """
    totareas = areas
    exfile = open(fname,'w')
    print >>exfile,";Written by dw_WriteBoundlist"
    print >>exfile,";'NodeID' 'Number' 'Type'"
    nr_inflowtypes = len(inflowtypes)
    
    #for i in range(nr_inflowtypes-1):
    #    totareas = vstack((totareas,areas))
    totareas = areas

    arid = 0
    for i in range(len(pointer)):
        if pointer[i,1] < 0:
            print >>exfile,"'BD_" + str(absolute(pointer[i,1])) +  "'  '" + str(absolute(pointer[i,1])) + "'" + " 'Outflow'"
        elif   pointer[i,0] < 0:
            #ar = int(absolute(totareas[arid]))
            ar = totareas[arid]
            print >>exfile,"'BD_" +str(absolute(pointer[i,0])) + "' " + "'" + str(absolute(pointer[i,0])) + "'" + " 'Area_" + str(ar) + "'"
            arid = arid + 1
                
    exfile.close()    
 

def dw_WritePointer(fname,pointer):
    """ 
    WRites the pointer file
    B4\_pointer.inc
    """
    exfile = open(fname,'w')
    print >>exfile,";Written by dw_WritePointer"
    print >>exfile,";nr of pointers is: ", str(pointer.shape[0])
    savetxt(exfile,pointer,fmt='%10.0f')
    exfile.close()   
            

def dw_WriteSegmentOrExchangeData(ttime,fname,datablock,boundids,WriteAscii=True):
    """
    Writes a segment/exchange data file
    
    
    
    Input:
        - time - time for this timestep  
        - fname - File path of the segment/exchange data file</param>
        - datablock - array with data
        - boundids to write more than 1 block
        - WriteAscii - set to 1 to alse make an ascii dump
        
    """
    # First convert the array to a 32 bit float
    totareas = datablock
    for i in range(boundids-1):
        totareas = vstack((totareas,datablock))
    
    artow= array(totareas,dtype=float32).copy()
    timear= array(ttime,dtype=int32)
    if os.path.isfile(fname): # append to existing file
        fp = open(fname,'ab')
        tstr = timear.tostring() + artow.tostring()
        fp.write(tstr) 
        if WriteAscii:
            fpa = open(fname+".asc",'a')
            timear.tofile(fpa,format="%d\t", sep=":")
            artow.tofile(fpa,format="%10.8f", sep="\t")
            fpa.write('\n')
    else:
        fp = open(fname,'wb')
        tstr = timear.tostring() + artow.tostring()
        fp.write(tstr)
        if WriteAscii:
            fpa = open(fname+".asc",'w')
            timear.tofile(fpa,format="%d\t", sep=":")
            artow.tofile(fpa,format="%10.8f", sep="\t") 
            fpa.write('\n')
        
    fp.close()
    if WriteAscii:
        fpa.close()



#TODO: Add exta column with boundary labels (of the inflows)

def dw_mkDelwaqPointers(ldd,amap,difboun,layers):
    """
    An ldd is used to determine the from-to relations for delwaq. 
    amap is used to link boundaries to the segments for delwaq (negative 
    numbers). These are area based boundaries. Diffboun is a 
    python dectionary with diffusive boundaries for each
    cell.
    
    Input:
        - ldd
        - map to determine the active points)
        - difboun : number of inflow boundaries per cell
        - layers [nr of soil layers (only vertical flow)]
        
    Output:
        - pointer, fromto, outflows, boundaries, segment
        - matrix with 4 colums: from to, zero, zero.
        - catchid

    .. note::  use savetxt("pointer.inc",pointer,fmt='%10.0f') to save this
        for use with delwaq
        
    .. todo:: 
        Add exta column with boundary labels (of the inflows)    
        
    """
    # Firts make sure there is at least on outflow in the model
    ptid = uniqueid(boolean(amap))
    flowto = downstream(ldd,ptid)
    # Fix if downsteam is no pit.In that case flowto is missing, set it so itself
    hasflowto = defined(flowto)
    flowto = ifthenelse(defined(ptid) != hasflowto, ptid, flowto)

    # find all upstream cells (these must be set negative)
    upbound = upstream(ldd,1.0)
    upbound = ifthen(amap > 0, upbound)
    # Find the lower boundaries (and pits). These flow to themselves
    
    
    # make into flatted numpy arrays
    np_ptid = pcr2numpy(ptid,NaN).flatten()
    np_flowto = pcr2numpy(flowto,NaN).flatten()
    np_catchid = pcr2numpy(scalar(amap),NaN).flatten()
    np_upbound = pcr2numpy(upbound,NaN).flatten()

    # remove all non-active cells
    np_catchid = np_catchid[np_catchid > 0.0]
    np_upbound = np_upbound[isfinite(np_upbound)]
    np_flowto = np_flowto[isfinite(np_flowto)]
    np_ptid = np_ptid[isfinite(np_ptid)]
    np_flowto= np_flowto.reshape(len(np_flowto),1)
    np_ptid= np_ptid.reshape(len(np_ptid),1)
    np_catchid= np_catchid.reshape(len(np_catchid),1) * -1
    # Now make catchid a list
    np_catchid = np_catchid.flatten()
    np_catchid = array(int_(np_catchid),dtype='|S').tolist()
    # find all downstream segments (flowto == ptid)
    # now set the flowto points (outflows, usually just one) also to negative
    lowerck = absolute(np_ptid) == absolute(np_flowto)
    # mak epointer matrix and add to zero zolumns
    orgpointer = hstack((np_ptid,np_flowto,zeros((len(np_flowto),1)),zeros((len(np_flowto),1))))
    pointer = orgpointer.copy()
    extraboun = []
    # Add the inflow boundaries here.
    cells = pointer[:,0]
    cells = cells.reshape(len(cells),1)
    bounid = cells.copy()
    zzerocol = zeros((len(np_flowto),1))
    inflowId = bounid.copy()
    
    
    # outflow to pointer
    # point -> - point
    lopt = np_ptid[lowerck]
    lopt = lopt.reshape(len(lopt),1)
    zerocol = zeros((len(lopt),1))
    lowerids = arange(1,len(lopt) + 1) * -1
    #of = hstack((lopt,lopt * -1.0,zerocol,zerocol))
    lowerids = lowerids.reshape(len(lowerids),1)
    of = hstack((lopt,lowerids,zerocol,zerocol))
    
    #pointer = vstack((pointer,of))
    
    # Now remove double pointer to itself and replace by lower boundary
    lowerck = pointer[:,0] == pointer[:,1]   
    pointer[lowerck,:] = of 
    start = absolute(lowerids.min()) + 1
    bouns = 1
    for idd in range(1,difboun + 1):
        inflowId[:] = idd
        bounid = arange(start,(len(cells)+start)).reshape((len(cells),1)) * -1.0
        if bouns == 1:
            extraboun = hstack((bounid,cells,zzerocol,zzerocol))        
        else:
            extraboun = vstack((extraboun,hstack((bounid,cells,zzerocol,zzerocol))))
        bouns = bouns +1
        start = start + len(cells)
    
    res = []
    for idd in range(1,difboun + 1):
        ct = list(np_catchid)
        print "ct: "
        print unique(ct)
        for i in range(0,len(np_catchid)):
            ct[i] = np_catchid[i] + "_" + str(idd)
        res = res + ct
    print unique(res)
    np_catchid = res
    #pointer = vstack((pointer,extraboun))
    # now catchment id's
    #zerocol = zeros((len(np_catchid),1))
    #extraboun= hstack((np_catchid,cells,zerocol,zerocol))
    #print np_catchid
    
    
    if len(extraboun) > 0:
        pointer = vstack((pointer,extraboun)) 
      
    return ptid, flowto, pointer, orgpointer[:,0], of[:,0:2], extraboun[:,0:1].flatten(), np_ptid.flatten(), np_catchid



def dw_pcrToDataBlock(pcrmap):
    """
    Converts a pcrmap to a numpy array.that is flattend and from which
    missing values are removed. Used for generating delwaq data
    """    
    ttar = pcr2numpy(pcrmap,NaN).flatten()
    ttar = ttar[isfinite(ttar)]
    
    return ttar
            

 
def readTS(name, ts):
    mname = os.path.basename(name)
    #  now generate timestep
    tsje = "%0.11d" % ts
    ff = mname + tsje[len(mname):]
    ff = ff[:8] + "." + ff[8:]
    name = os.path.dirname(name) + "/" + ff
    mapje = readmap(name)
    
    return mapje

def dw_CreateDwRun(thedir):
    """"
    create the dir to save delwaq info in
    """
    if not os.path.isdir(thedir):
        os.makedirs(thedir + "/fixed/")
        os.makedirs(thedir + "/includes_deltashell/")
        os.makedirs(thedir + "/includes_flow/")
        os.makedirs(thedir + "/debug/")
    if os.path.exists(thedir + "/includes_flow/area.dat"):
        os.remove(thedir + "/includes_flow/area.dat")
    if os.path.exists(thedir + "/includes_flow/flow.dat"):
        os.remove(thedir + "/includes_flow/flow.dat")
    if os.path.exists(thedir + "/includes_flow/volume.dat"):
        os.remove(thedir + "/includes_flow/volume.dat")
    if os.path.exists(thedir + "/includes_flow/length.dat"):
        os.remove(thedir + "/includes_flow/length.dat")
    if os.path.exists(thedir + "/includes_flow/area.dat.asc"):
        os.remove(thedir + "/includes_flow/area.dat.asc")
    if os.path.exists(thedir + "/includes_flow/flow.dat.asc"):
        os.remove(thedir + "/includes_flow/flow.dat.asc")
    if os.path.exists(thedir + "/includes_flow/volume.dat.asc"):
        os.remove(thedir + "/includes_flow/volume.dat.asc")
    if os.path.exists(thedir + "/includes_flow/length.dat.asc"):
        os.remove(thedir + "/includes_flow/length.dat.asc")        


def dw_Write_Times(dwdir,T0,timeSteps,timeStepSec):
    """
    Writes B1_T0.inc, B2_outputtimers.inc, B2_sysclock.inc and /B2_simtimers.inc
    Assumes daily timesteps for now!
    """
    # B1_T0.inc
    exfile = open(dwdir + "/B1_T0.inc",'w')
    print >>exfile, "\'T0: " + T0.strftime("%Y.%m.%d %H:%M:%S") + "  (scu=       1s)\'"
    exfile.close()
    # B2_outputtimers.inc
    timeRange  = timedelta(seconds=timeStepSec * timeSteps)
    exfile = open(dwdir + "/B2_outputtimers.inc",'w')
    etime = T0 + timeRange
    print >>exfile, "  " + T0.strftime("%Y/%m/%d-%H:%M:%S") + "  " + etime.strftime("%Y/%m/%d-%H:%M:%S") + "  001000000"
    print >>exfile, "  " + T0.strftime("%Y/%m/%d-%H:%M:%S")  + "  " + etime.strftime("%Y/%m/%d-%H:%M:%S") + "  001000000"
    print >>exfile, "  " + T0.strftime("%Y/%m/%d-%H:%M:%S")  + "  " + etime.strftime("%Y/%m/%d-%H:%M:%S") + "  001000000"  
    exfile.close()
    #B2_simtimers.inc
    exfile = open(dwdir + "/B2_simtimers.inc",'w')
    print >>exfile, "  " + T0.strftime("%Y/%m/%d-%H:%M:%S")
    print >>exfile, "  " + etime.strftime("%Y/%m/%d-%H:%M:%S")
    print >>exfile, "  0 ; timestep constant"
    print >>exfile, "; dddhhmmss format for timestep"
    print >>exfile,"  001000000 ; timestep"
    exfile.close()
    exfile = open(dwdir + "/B2_sysclock.inc",'w')
    print >>exfile,"  86400 \'DDHHMMSS\' \'DDHHMMSS\'  ; system clock"
    exfile.close()


def dw_Write_Substances(fname,areas):
    """
    Writes the B1_sublist.inc file
    input:
        
        it writes substances for the areas and an initial and mass balance 
        check substance
        
    """

    exfile = open(fname,'w')
    areas = sorted(areas,reverse=True)
    print >>exfile,"; number of active and inactive substances"
    print >>exfile,"%d         0" % (len(areas) + 2)
    print >>exfile,"; active substances"
    print >>exfile, "1             \'Initial\' ; "
    print >>exfile, "2             'Check' ; "
    j = 2
    for i in areas:
        j = j + 1
        print >>exfile, "%d            \'Area_%s\'" %  (j,i)
    print >>exfile,"; passive substances"
    
        
    exfile.close()
    
    
def dw_Write_B2_outlocs(fname,gauges,segs):
    """
    Write an output loc file based on the wflow_gauges
    map.
    """
    segs = ifthenelse(gauges > 0,segs,NaN)
    gauges = ifthenelse(gauges > 0,scalar(gauges),NaN)
    np_gauges = pcr2numpy(gauges,NaN).flatten()
    np_segs = pcr2numpy(segs,NaN).flatten()
        
    np_gauges = np_gauges[isfinite(np_gauges)]
    np_segs = np_segs[isfinite(np_segs)]
    
    if len(np_segs) != len(np_gauges):
        logger.error("Gauges and segments do not match!")

    pts = size(np_segs)
    exfile = open(fname,'w')
    print >>exfile,"%d ; nr of locations" % pts
    print >>exfile,"; \'outlocname\' numberofsegments segment list"
    i = 0
    for loc in np_gauges:
        print >>exfile," \'%d\' 1 %d" % (loc, np_segs[i])
        i = i + 1
    exfile.close()


   

def usage(*args):
    sys.stdout = sys.stderr
    for msg in args: print msg
    print __doc__
    sys.exit(0)
                
pointer = ""
#TODO: Allow multiple sources. Now only inwater actually works!



def main():
    caseId = "Ahr_DW/"
    caseId = "default_hbv"
    runId = "run_default"
    dwdir = "dw_rhine"
    areamap = "staticmaps/wflow_subcatch.map"
    timeSteps = 1
    timeStepSec = 86400
    sourcesMap = []
    fewsrun = False
    WriteAscii=False
    Write_Dynamic= False
    T0 = datetime.strptime("2000-01-01 00:00:00",'%Y-%m-%d %H:%M:%S')

    try:
        opts, args = getopt.getopt(sys.argv[1:], 'dD:C:R:S:hT:F:s:O:A:')
    except getopt.error, msg:
        pcrut.usage(msg)
    
    for o, a in opts:
        if o == '-F': 
            runinfoFile = a
            fewsrun = True
        if o == '-C':caseId = a
        if o == '-R': runId = a
        if o == '-D': dwdir = a
        if o == '-d': Write_Dynamic= True
        if o == '-s': timestepsecs = int(a)
        if o == '-S': sourcesMap.append(a)
        if o == '-h': usage()
        if o == '-T': timeSteps = int(a)
        if o == '-A': areamap = a
        if o == '-O': T0 = datetime.strptime(a,'%Y-%m-%d %H:%M:%S')
        
    

   
    global pointer
    dw_CreateDwRun(dwdir)
    
    if fewsrun: 
        timeSteps =  wflow_adapt.getTimeStepsfromRuninfo(runinfoFile) * 86400/timestepsecs
        T0 =  wflow_adapt.getStartTimefromRuninfo(runinfoFile)
    
    #: we need one delwaq calculation timesteps less than hydrology
    # timeSteps = timeSteps # need one more hydrological timestep as dw timestep
    firstTimeStep = 0
        
        
    logger = pcrut.setlogger(dwdir + "/debug/wflow_delwaq.log","wflow_delwaq") 
    #caseid = "default_hbv"
    logger.info("T0 of run: " + str(T0))
    boundids = len(sourcesMap)  # extra number of exchnages for all bounds

    #Number of exchnages is elements minus number of outflows!!  
    
    
    # Get subcatchment data
    logger.info("Reading basemaps")
    setclone(caseId + "/staticmaps/wflow_subcatch.map")
    amap = scalar(readmap(caseId + "/" + areamap))
        # get ldd
    ldd = readmap(caseId + "/staticmaps/wflow_ldd.map")
    gauges = readmap(caseId + "/staticmaps/wflow_gauges.map")

    cellsize = float(pcr2numpy(readmap(caseId + "/" + runId + "/outsum/rl.map"),NaN)[0,0])
    logger.info("Cellsize model: " + str(cellsize))
    
    amap = ifthen(amap > 0, amap)
    ldd = ifthen(amap > 0, ldd)
    report(amap,dwdir +"/debug/area.map")
    report(ldd,dwdir +"/debug/ldd.map")
    
    thecells = pcr2numpy(amap,NaN).flatten()
    nrcells = len(thecells)
    nractcells = len(thecells[isfinite(thecells)])
    
    logger.info("Total number gridcells (including inactive): " + str(nrcells))        
    logger.info("Total number of used gridcells: " + str(nractcells))
    
        # find all upstream cells (these must be set negative)
    upbound = upstream(ldd,1.0)
    upbound = ifthen(upbound == 0, upbound)
    upar=pcr2numpy(scalar(upbound),NaN).flatten()
    logger.info("Number of upstream cells (without upstream connection): " + str(len(upar[isfinite(upar)])))
    report(upbound,dwdir +"/debug/upbound.map")
    
    # get pointer an boundaries from ldd, subcatch and defined boundaries (P only now)
    ptid, flowto, pointer, fromto, of , bounds, segments, areas = dw_mkDelwaqPointers(ldd,amap,boundids,1)
    
    save(dwdir +"/debug/pointer.npy",pointer)
    save(dwdir +"/debug/fromto.npy",fromto)
    save(dwdir +"/debug/of.npy",of)
    save(dwdir +"/debug/bounds.npy",bounds)
    save(dwdir +"/debug/segments.npy",segments)
    save(dwdir +"/debug/areas.npy",areas)
    
    # Write id maps to debug area
    report(ptid,dwdir + "/debug/ptid.map")
    report(flowto,dwdir + "/debug/flowto.map")
    logger.info("Unique areas: " + str(unique(areas)))
    #logger.info("Number of area inflows: " + str(len(areas) * boundids))
    logger.info("Number of segments: " + str(len(segments.flatten())))
    logger.info("Number of internal flows: " + str(len(fromto.flatten())))
    logger.info("outflow  ids: " + str(of))
    logger.info("source maps: " + str(sourcesMap))    
    
    NOOF = of.shape[0]
    NOSQ = segments.shape[0]
    NOQ = pointer.shape[0]
    
    dw_WriteNrSegments(dwdir + "/includes_deltashell/B3_nrofseg.inc",NOSQ)
    # Write pointer file
    #TODO: add sources maps here (no only one source supported)
    dw_WritePointer(dwdir + "/includes_deltashell/B4_pointer.inc",pointer)
    # Write the number of exchanges
    dw_WriteNrExChnages(dwdir + "/includes_deltashell/B4_nrofexch.inc",NOQ)
    dw_WriteBoundlist(dwdir + "/includes_deltashell/B5_boundlist.inc",pointer,areas,of,sourcesMap)
    dw_WriteBoundData(dwdir + "/includes_deltashell/B5_bounddata.inc",unique(areas))

    dw_WriteInitials(dwdir + "/includes_deltashell/B8_initials.inc",sourcesMap)
    dw_Write_Substances(dwdir + "/includes_deltashell/B1_sublist.inc",unique(areas))
    dw_Write_Times(dwdir + "/includes_deltashell/",T0,timeSteps-1,timeStepSec)
    dw_Write_B2_outlocs(dwdir + "/includes_deltashell/B2_outlocs.inc",gauges,ptid)
    
    
    # Creat dummy area: number of exchnages all set to 1.0
    areas = zeros(pointer.shape[0]) + 1.0
    # create dummy length file
    length_block = zeros(pointer.shape[0] * 2) + 0.5
    # write  length file
    logger.info("Writing length.dat. Nr of points: " + str(size(length_block)))
    dw_WriteSegmentOrExchangeData(0,dwdir + '/includes_flow/length.dat',length_block,1,WriteAscii)
    

    
    ts = 1
    
    if Write_Dynamic:       
        for i in range(firstTimeStep,timeSteps * timeStepSec,timeStepSec):
            volume_map = readTS(caseId + "/" + runId + "/outmaps/" + volumeMapStack,ts)
            volume_block = dw_pcrToDataBlock(volume_map)
            
            # volume for each timestep and number of segments
           
            logger.info("Writing volumes.dat. Nr of points: " + str(size(volume_block)))
            dw_WriteSegmentOrExchangeData(i,dwdir + '/includes_flow/volume.dat',volume_block,1,WriteAscii)
            
            # Now write arreas  
            
            logger.info("Writing areas.dat. Nr of points: " + str(size(areas)) + " boundid: " + str(boundids))
            dw_WriteSegmentOrExchangeData(i,dwdir + '/includes_flow/area.dat',areas,1,WriteAscii)
        
            
            
            # Now write the flows (exchnages)
            # First read the flows in the kinematic wave reservoir (internal exchnages)
            flow = readTS(caseId + "/" + runId + "/outmaps/" + runoffMapStack,ts)
            flow_block_Q = dw_pcrToDataBlock(flow)
            # now the inw
            flowblock = flow_block_Q
            
            # Now read the inflows in each segment (water that enters the kinamatic 
            # wave reservoir)
            for source in sourcesMap:
                logger.info("Step: " + str(ts) + " source: " + str(source))
                thesource = readTS(caseId + "/" + runId + "/outmaps/" + source ,ts)
                flow_block_IN = dw_pcrToDataBlock(thesource)
                flowblock = hstack((flowblock,flow_block_IN))
                
            logger.info("Writing flow.dat. Nr of points: " + str(size(flowblock)))
            dw_WriteSegmentOrExchangeData(i,dwdir + '/includes_flow/flow.dat',flowblock,1,WriteAscii)
            
            ts = ts + 1
        
        """
        Write last volume block with current kinwavevol
        """  
        ts = ts -1    
        i = i + 1
        logger.info("Writing last step..")
        
        logger.info("Writing areas.dat. Nr of points: " + str(size(areas)) + " boundid: " + str(boundids))
        dw_WriteSegmentOrExchangeData(i,dwdir + '/includes_flow/area.dat',areas,1,WriteAscii)
        logger.info("Writing flow.dat. Nr of points: " + str(size(flowblock)))
        dw_WriteSegmentOrExchangeData(i,dwdir + '/includes_flow/flow.dat',flowblock,1,WriteAscii)
 
        volume_map = readTS(caseId + "/" + runId + "/outmaps/voln",ts)
        volume_block = dw_pcrToDataBlock(volume_map)
        logger.info("Writing volumes.dat. Nr of points: " + str(size(volume_block)))
        dw_WriteSegmentOrExchangeData(i,dwdir + '/includes_flow/volume.dat',volume_block,1,WriteAscii)
            
    
    
if __name__ == "__main__":
    main()