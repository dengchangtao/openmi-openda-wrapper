using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using OpenDA.DotNet.Interfaces;
using OpenDA.DotNet.OpenMI.Bridge;
using org.openda.application;
using java.io;
using MikeSheInOpenDA;
using System.IO;

namespace startRun
{
    class Program
    {
        static void Main(string[] args)
        {

            //const string mikeSheConfigFile = @"c:\OpenDA_MI\Test_5x5\MikeSheConfig.txt";
            const string mikeSheConfigFile = @"C:\OpenDA_MI\Karup\XML\MikeSheConfig.txt";

            //const string odaDirectoryPath = @"c:\OpenDA_MI\Test_5x5";
            const string odaDirectoryPath = @"C:\OpenDA_MI\Karup\XML";


            const string odaFileName = "EnSR_test1.oda";
            //const string odaFileName = "EnKF_test1.oda";

            
            if(!System.IO.File.Exists(mikeSheConfigFile))
            {
                
                throw new System.IO.FileNotFoundException(mikeSheConfigFile);
            }

           

            MikeSheOpenMIModelFactory mikeSheOpenMIModelFactory = new MikeSheOpenMIModelFactory();
            mikeSheOpenMIModelFactory.Initialize(Path.GetDirectoryName(mikeSheConfigFile), new[] { Path.GetFileName(mikeSheConfigFile) });

            OpenDA.DotNet.OpenMI.Bridge.ModelFactory.InsertModelFactory(mikeSheOpenMIModelFactory);

            ModelFactory openDaModelFactory = new ModelFactory();
            openDaModelFactory.Initialize(null, null);

            //IModelInstance modelInstance = openDaModelFactory.GetInstance(new string[] { }, outputLevel: 0);

            ApplicationRunnerSingleThreaded applicationRunner = new ApplicationRunnerSingleThreaded();


            applicationRunner.runSingleThreaded(new java.io.File(odaDirectoryPath), odaFileName);

            System.Console.WriteLine("Done. Hit a key!");
            System.Console.ReadKey();
        }
    }
}
