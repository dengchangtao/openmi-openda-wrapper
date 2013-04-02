﻿using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Diagnostics;
using DHI.OpenMI2.Wrappers.EngineWrapper;
using DHI.OpenMI2.Sdk.Backbone;
using OpenDA.DotNet.AdditionalInterfaces;
using OpenMI.Standard2.TimeSpace;
using System.Xml.Linq;
using System.Xml;
using System.IO;
using DHI.OpenMI2.MikeShe;
using DHI.DHIfl;
using OpenMI.Standard2;
using OpenDA.DotNet.OpenMI.Bridge;


namespace MikeSheInOpenDA
{
    public class MikeSheOpenMIModelFactory : IOpenDaModelProvider
    {

        private static int _instanceCounter;
        private string _msheFileName;
        private string _mikeSheConfigDa;
        private bool _copyModels;
        private IList<string> _exchangeItems;
        private MikeSheOpenMITimespaceComponentExtensions _mshe;

        /// <summary>
        /// The path should point to where the OMI-file is located.
        /// The first args is the omi file name without path.
        /// </summary>
        /// <param name="workingDirPath"></param>
        /// <param name="args"></param>
        public void Initialize(string workingDirPath, params string[] args)
        {

            /* Read OpenMI MikeShe specific configuration
               * name and directory of the Mike-She inputfile 
               * time configuration (precicion of time handling) (may-be from the java side)
               * buffering of exchageItems
               * whether or not to copy the models 
               * which exchangeItems to consider
            
               <MikeSheConfig>
                  <inputFile>C:\some\path\model.she</inputFile>
                  <copyModels>false</copyModels>
                  <useExchangeItems>Name1;Name2;Name3</useExchangeItems>
               </MikeSheCongig>
             */


            if (args[0] != null && string.Compare("", workingDirPath) != 0 )
            {
                _mikeSheConfigDa = Path.Combine(workingDirPath, args[0]);
            }
            else
            {
                throw new Exception("Initialize requires a working path that is not null or blank & a file name");
            }

            if(!File.Exists(_mikeSheConfigDa))
            {
                throw new FileNotFoundException(_mikeSheConfigDa);
            }

            

            
            //Get the she file name
            Hashtable properties = new Hashtable();
            XmlTextReader reader = new XmlTextReader(_mikeSheConfigDa);
            while (reader.Read())
            {

                switch ( reader.LocalName )
                {
                   case "inputFile":
                        _msheFileName = reader.ReadElementString().Trim();
                        if(!File.Exists(_msheFileName))
                        {
                            throw new FileNotFoundException("File Not found in the MikeConfiguration in inputFile", _msheFileName);
                        }
                        break;

                   case "copyModels":

                        
                        string cpModels = reader.ReadElementString().Trim();
                        if(System.String.CompareOrdinal(cpModels, "true") == 0)
                        {
                            _copyModels = true;
                        }
                        else if(System.String.CompareOrdinal(cpModels, "false") == 0)
                        {
                            _copyModels = false;
                        }
                        else
                        {
                            throw new Exception("in MikeConfiguration, copyModels must be either 'true' or 'false'. ");
                        }
                        break;


                   case "useExchangeItems":
                        _exchangeItems = new List<string>();
                        string[] words = reader.ReadElementString().Trim().Split(';');
	                    foreach (string word in words)
	                    {
	                        _exchangeItems.Add(word.Trim());
	                    }
                        if(_exchangeItems.Count<1)
                            {throw new Exception("in MikeConfiuration, must specify possible exchange items used seperated by semicolon.");}
                        break;

                }
            }
        }

        public ITimeSpaceComponent CreateInstance()
        {
            string newSheFileName = "";
            if(_copyModels)
            {
                throw new NotImplementedException("Copy model's not implemented yet. Check the MikeSheConfig txt file. ");
                // Copy Entire 'base' directory to a new one in the same root directory with instancenumber name   
                DirectoryInfo sourceDirectory = Directory.GetParent(_msheFileName);
                DirectoryInfo targetDirectory = Directory.GetParent(Directory.GetParent(sourceDirectory.ToString()) + "\\" + _instanceCounter + "\\" + _instanceCounter);
                CopyDirectory(sourceDirectory, targetDirectory);

                newSheFileName = targetDirectory.ToString() + "\\" + Path.GetFileName(_msheFileName);
            }
            else if (!_copyModels)
            {

                string parentPath = new DirectoryInfo(_msheFileName).Parent.Parent.FullName.ToString();
                string filename = Path.GetFileName(_msheFileName);
                newSheFileName = Path.Combine(parentPath, "Ensembles", _instanceCounter.ToString(), filename);

                if (!File.Exists(newSheFileName))
                {
                    Console.WriteLine(" ****************\n ");
                    Console.WriteLine("In ModelAccess.cs at InitializeModel \n");
                    Console.WriteLine("\n  Error in Model Instance --> {0}\n", _instanceCounter);
                    Console.WriteLine("\n  File Not Found --> {0}\n", _instanceCounter);
                    throw new Exception("Could not find the model .she file in ModelOpenMI2");
                }

                /*
                // Not Copying models
                DirectoryInfo sourceDirectory = Directory.GetParent(_msheFileName);
                DirectoryInfo targetDirectory = Directory.GetParent(Directory.GetParent(sourceDirectory.ToString()) + "\\" + _instanceCounter + "\\" + _instanceCounter);
                newSheFileName = targetDirectory.ToString() + "\\" + Path.GetFileName(_msheFileName);
                */



            }
            else
            {
                throw new Exception("Copy models or not - unspecified.");
            }

            if(!File.Exists(newSheFileName))
            {
                throw new FileNotFoundException("Mike She file not found: ", newSheFileName);
            }

            //Preprocess mshe before initializing. Should have put this in the engine years ago
            string path;
            DHIRegistry key = new DHIRegistry(DHIProductAreas.COMMON_COMPONNETS, false);
            key.GetHomeDirectory(out path);
            Process runner = new Process();
            runner.StartInfo.FileName = System.IO.Path.Combine(path, "Mshe_preprocessor.exe");
            runner.StartInfo.Arguments = newSheFileName;
            //ZZZ Change the Working Directory necessary for the SVAT
            runner.StartInfo.WorkingDirectory = System.IO.Path.GetDirectoryName(newSheFileName);
            runner.StartInfo.UseShellExecute = false;
            runner.Start();
            runner.WaitForExit();

            _instanceCounter++;

            _mshe = new MikeSheOpenMITimespaceComponentExtensions();

            IList<IArgument> prop = _mshe.Arguments;

            prop.UpdateValue("SetupPath", Path.GetDirectoryName(newSheFileName));
            prop.UpdateValue("SetupFileName", Path.GetFileName(Path.GetFileName(newSheFileName)));
            prop.UpdateValue("ForceRemote", true);
            try
            {
                _mshe.Initialize();
            }
            catch (System.Exception excep)
            {
                Console.WriteLine("Problem with Model Initialization. ");
                Console.WriteLine(excep.Message);
            }

            // For OpenMI 2.0 we need to add a consumer ( a dummy in this case ) for each Output
            // Exchange Item. 
            Input dummyInput2 = new Input("dummyIn");
            List<string> outToRemove = new List<string>();
            
            for (int i = 0; i < _mshe.EngineOutputItems.Count; i++)
            {
                if (_exchangeItems.Contains(_mshe.EngineOutputItems[i].Id))
                {
                    _mshe.EngineOutputItems[i].AddConsumer(dummyInput2);
                }
                else
                {
                    outToRemove.Add(_mshe.EngineOutputItems[i].Id);
                }
            }
            outToRemove = outToRemove.Distinct().ToList();
            while (_mshe.EngineOutputItems.Count != _exchangeItems.Count)
            {

                _mshe.EngineOutputItems.Remove(_mshe.EngineOutputItems.First(s => outToRemove.Contains(s.Id)));
            }

            // For OpenMI 2.0 we need to add a consumer ( a dummy in this case ) for each Output
            // Exchange Item. 
            List<string> inToRemove = new List<string>();

            for (int i = 0; i < _mshe.EngineInputItems.Count; i++)
            {
                if (!_exchangeItems.Contains(_mshe.EngineInputItems[i].Id))
                {
                    inToRemove.Add(_mshe.EngineInputItems[i].Id);
                }
            }
            inToRemove = inToRemove.Distinct().ToList();
            while (_mshe.EngineInputItems.Count != _exchangeItems.Count)
            {

                _mshe.EngineInputItems.Remove(_mshe.EngineInputItems.First(s => inToRemove.Contains(s.Id)));
            }

            try
            {
                _mshe.Prepare();
            }
            catch (System.Exception excep)
            {
                string err = "Problem with Model Initialization/Prepare. Ensure \n\t(1) .she file exisits and is not being used by any other process or service (like licserv) \n\t(2) MIKE is licensed correctly & licserv is running correctly \n\t(3) The .she file and associated model files are not read only \n\t(4) The MikeShe file can run on it's own (no negative rainfall, incompatible options etc.). \n";
                Console.WriteLine(" ****************\n ");
                Console.WriteLine("In ModelAccess.cs at InitializeModel \n");
                Console.WriteLine("\n  Error in Model Instance --> {0}\n", _instanceCounter);
                Console.WriteLine(err);
                Console.WriteLine(excep.Message);
                Console.WriteLine(" ****************\n ");
                string exceptionString = "\n\n EXIT - problem with model initilizatoin. \n Error in Model Instance --> " + _instanceCounter.ToString() + "\n";
                throw new Exception(exceptionString);
            }

            _mshe.Update();

            //int startin = 60 - DateTime.Now.Second;
            //System.Threading.TimerCallback c;
            //ThreadCallBack = new System.Threading.Timer(o => Console.WriteLine("  ------> working: i=" + _instanceCounter.ToString() + " time: " + _mshe.CurrentTime.ToDateTime()), null, startin * 1000, 30000);

            return _mshe;


        }

        // CALL BACK
        private System.Threading.Timer ThreadCallBack { get; set; }

        public void SaveInstance(ITimeSpaceComponent timeSpaceComponent)
        {
            //throw new NotImplementedException();
        }


        static void CopyDirectory(DirectoryInfo source, DirectoryInfo destination)
        {
            if (!destination.Exists)
            {
                try
                {
                    destination.Create();

                    // Copy all files.
                    FileInfo[] files = source.GetFiles();
                    foreach (FileInfo file in files)
                    {
                        file.CopyTo(Path.Combine(destination.FullName,
                            file.Name));
                    }

                    // Process subdirectories.
                    DirectoryInfo[] dirs = source.GetDirectories();
                    foreach (DirectoryInfo dir in dirs)
                    {
                        // Get destination directory.
                        string destinationDir = Path.Combine(destination.FullName, dir.Name);

                        // Call CopyDirectory() recursively.
                        CopyDirectory(dir, new DirectoryInfo(destinationDir));
                    }
                }
                catch
                {
                    Console.WriteLine("Problem Copying Directory from: {0}   to  {1}", source, destination);
                }
            }

        }


    }
}
