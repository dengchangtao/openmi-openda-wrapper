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
            string mikeSheConfigFile = "";
            string odaDirectoryPath = "";
            string odaFileName = "";
            // Args Requires 2 fields:
            // 1) the MikeSHEConfig.txt
            // 2) the .oda file
            if (args[0] != null && args[1] != null)
            {

                if( System.IO.File.Exists( args[0] ) && System.IO.File.Exists( args[1] ) )
                {
                    mikeSheConfigFile = args[0];

                    odaFileName = System.IO.Path.GetFileName(args[1]);
                    odaDirectoryPath = System.IO.Path.GetDirectoryName(args[1]);
                }
                else
                {
                    throw new System.IO.FileNotFoundException(" One or more of the files were not found \n" + args[0] +
                                                              "\n" + args[1]);
                }
            }
            else
            {
                // Args Requires 2 fields:
                // 1) the MikeSHEConfig.txt
                // 2) the .oda file
                System.Console.WriteLine("Takes two args 1) the mikeSHEConfig.txt   and   2) the OpenDA  .oda file.\n"  );
            }




            //const string mikeSheConfigFile = @"c:\OpenDA_MI\Test_5x5\MikeSheConfig.txt";
            // const string mikeSheConfigFile = @"C:\work\SOLProjects\11810549_HydroCast\Karup\XML\MikeSheConfig.txt";

            //const string odaDirectoryPath = @"c:\OpenDA_MI\Test_5x5";
            // const string odaDirectoryPath = @"C:\work\SOLProjects\11810549_HydroCast\Karup\XML";


            //const string odaFileName = "EnSR_test1.oda";
            // const string odaFileName = "EnKF_test1.oda";

            

           

            MikeSheOpenMIModelFactory mikeSheOpenMIModelFactory = new MikeSheOpenMIModelFactory();
            mikeSheOpenMIModelFactory.Initialize(Path.GetDirectoryName(mikeSheConfigFile), new[] { Path.GetFileName(mikeSheConfigFile) });

            OpenDA.DotNet.OpenMI.Bridge.ModelFactory.InsertModelFactory(mikeSheOpenMIModelFactory);

            ModelFactory openDaModelFactory = new ModelFactory();
            openDaModelFactory.Initialize(null, null);

            ApplicationRunnerSingleThreaded applicationRunner = new ApplicationRunnerSingleThreaded();


            applicationRunner.runSingleThreaded(new java.io.File(odaDirectoryPath), odaFileName);

            System.Console.WriteLine("Done. Hit a key!");
            System.Console.ReadKey();
        }
    }
}
