using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using MikeSheInOpenDA.Spatial;
using OpenDA.DotNet.OpenMI.Bridge;
using OpenMI.Standard2;
using OpenMI.Standard2.TimeSpace;
using DHI.OpenMI2.Sdk.Spatial;
using DHI.OpenMI2.Sdk.Backbone;

namespace MikeSheInOpenDA
{
    public class MikeSheOpenMITimespaceComponentExtensions : DHI.OpenMI2.MikeShe.WMEngineAccess,ITimeSpaceComponentExtensions  
    {
        public ITime currentTime()
        {
            return this.CurrentTime;
        }

        /// <summary>
        /// For easy observation handling a model can provide some additional exhange items  especially for model predictions at observation locations
        /// </summary>
        /// <returns></returns>
        public IList<OpenDA.DotNet.Interfaces.IExchangeItem> getAdditionalExchangeItem()
        {
            IList<OpenDA.DotNet.Interfaces.IExchangeItem> additional = new List<OpenDA.DotNet.Interfaces.IExchangeItem>();
            // We will hack something here!

            return additional;
        }


        /// <summary>
        /// OpenMI does not know about localization. In this method, the user can implement localization for OpenMI models in this method
        /// </summary>
        /// <param name="ExchangeItemID"></param>
        /// <param name="observationDescriptions"></param>
        /// <param name="distance"></param>
        /// <returns></returns>
        public double[] getLocalization(string exchangeItemId, OpenDA.DotNet.Interfaces.IObservationDescriptions observationDescriptions, double distance)
        {
            Console.WriteLine("\n ************** GET THE PROPER VALUES!!!!!!!!!!!!!!! ************** \n");
            IXYLayerPoint obsPoint = new XYLayerPoint(250.0, 250.0, 0);

            return GaussianLocalization(_mshe, exchangeItemId, obsPoint, distance);
            throw new NotImplementedException("figure out how to get the coordinates from the observation description.");
        }

        #region PrivateMethods
        private readonly DHI.OpenMI2.MikeShe.WMEngineAccess _mshe;

        /// <summary>
        /// Creates a dictionary with key equal to the model state index and the value the spatial information of that state index.
        /// </summary>
        /// <param name="gType">The geometric type of the exchange itme (2d or 3d)</param>
        /// <param name="baseOut">The exchange item base output</param>
        /// <param name="elementID">the string id of the exchange item.</param>
        /// <returns></returns>
        private IDictionary<int, ISpatialDefine> ModelCoordinates3D(GeometryTypes gType, IBaseOutput baseOut, string elementID)
        {
            IDictionary<int, ISpatialDefine> modelEntities = new Dictionary<int, ISpatialDefine>();
            int elementIDNumber;
            int n;

            try
            {
                elementIDNumber = _mshe.WMEngine.GetElementCount(elementID);
                n = baseOut.ElementSet().ElementCount;
            }
            catch
            {
                Console.WriteLine("\nElement {0} does not found in the model\n", elementID);
                throw new Exception("\nProblem in Model Instance - unable to find exchange item\n");
            }

            //int numBaseGrid = Convert.ToInt32(Math.Floor((double)n / (double)_mshe.WMEngine.NumberOfSZLayers));

            for (int i = 0; i < n; i++)
            {
                XYPolygon modelpolygon = ElementMapper.CreateXYPolygon(baseOut.ElementSet(), i);
                int zLayer = Convert.ToInt32(i % _mshe.WMEngine.NumberOfSZLayers);

                // Points in Polygon are defined as LL, LR, UR, UL  (l/l = lower/left, u = upper, r = right )
                // Finds the mid x and mid y point in the polygon (assuming rectangular grid)
                IXYLayerPoint min = new XYLayerPoint(modelpolygon.GetX(0), modelpolygon.GetY(0), zLayer);
                IXYLayerPoint max = new XYLayerPoint(modelpolygon.GetX(1), modelpolygon.GetY(3), zLayer);

                modelEntities.Add(i, new SpatialDefine(min, max, GeometryTypes.Geometry3D));
            }

            return modelEntities;
        }

        /// <summary>
        /// Returns a List of ISpatialDefine with assumed 90deg angles. 
        /// The ISpatialDefine is defined by two coordinates and a layer integer.
        /// A ISpatialDefine can represent a cuboid, rectangle or a point. 
        /// </summary>
        /// <param name="elementID"></param>
        /// <returns></returns>
        private IDictionary<int, ISpatialDefine> ModelCoordinates(string elementID)
        {

            IBaseLinkableComponent linkableComponent = _mshe;

            IBaseOutput baseOut = linkableComponent.Outputs.First(vID => string.Compare(vID.Id, elementID) == 0);

            char[] delimiterChars = { ',' };
            string[] words = baseOut.Description.Split(delimiterChars);
            string gridTypewords = words[1].Trim();

            // Default;
            GeometryTypes gType = GeometryTypes.GeometryPoint;

            if (string.Compare(gridTypewords, "SZ3DGrid", 0) == 0)
            {
                gType = GeometryTypes.Geometry3D;
            }
            else if (string.Compare(gridTypewords, "BaseGrid", 0) == 0)
            {
                gType = GeometryTypes.Geometry2D;
            }
            else
            {
                throw new Exception("Other types do exisit (UZ...)");
            }

            if (gType == GeometryTypes.Geometry3D)
            {
                return ModelCoordinates3D(gType, baseOut, elementID);
            }
            else
            {
                throw new Exception("The Rest not Implemented");
            }

        }

        /// <summary>
        ///  Calculates Gaussian Localization Mask.
        /// Given a point on a grid, return an array of doubles (of the same size as the grid) with localization values between 0 and 1.
        /// </summary>
        /// <param name="_modelInstance">A model instance from WMengine</param>
        /// <param name="_elementSetID">Exchange item string id</param>
        /// <param name="_point">point around which to calcualte the licalization mask</param>
        /// <param name="_locDistance">the distance radius of the Gaussian mask</param>
        /// <returns></returns>
        private double[] GaussianLocalization(DHI.OpenMI2.MikeShe.WMEngineAccess _modelInstance, string _elementSetID, IXYLayerPoint _point, double _locDistance)
        {
            IDictionary<int, ISpatialDefine> modelCoord = ModelCoordinates(_elementSetID);

            double[] localized2D = new double[modelCoord.Count];

            if (XYZGeometryTools.IsPointInModelPlain(_point, modelCoord))
            {
                for (int i = 0; i < modelCoord.Count; i++)
                {
                    if (Convert.ToInt32(_point.Layer) == modelCoord[i].Layer)
                    {
                        double distance = XYZGeometryTools.CalculatePointToPointDistance2D(modelCoord[i].MidPoint, _point);
                        localized2D[i] = normalCooefs(distance, _locDistance);
                    }
                }
            }
            return localized2D;
        }



        /// <summary>
        /// Distance to Normal calculator.
        /// Returns the Gaussian normalized distances.
        /// </summary>
        /// <param name="dist"> array of doubles of distances to each other </param>
        /// <param name="radius"> radius factor </param>
        /// <returns></returns>
        private double normalCooefs(double dist, double radius)
        {
            // Calculated result saved into iteself
            return Math.Exp(-0.5 * Math.Pow((dist / radius), 2));
        }


        #endregion PrivateMethods

    }
}
