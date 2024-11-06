package com.milelog

import kotlin.math.*

class BoundingBoxCalculator {

    data class MapPoint(
        var longitude: Double, // In Degrees
        var latitude: Double // In Degrees
    )

    data class BoundingBox(
        var minPoint: MapPoint,
        var maxPoint: MapPoint
    )

    companion object {
        // Semi-axes of WGS-84 geoidal reference
        private const val WGS84_A = 6378137.0 // Major semiaxis [m]
        private const val WGS84_B = 6356752.3 // Minor semiaxis [m]

        // 'halfSideInKm' is the half length of the bounding box you want in kilometers.
        fun getBoundingBox(point: MapPoint, halfSideInKm: Double): BoundingBox {
            val lat = deg2rad(point.latitude)
            val lon = deg2rad(point.longitude)
            val halfSide = 1000 * halfSideInKm

            // Radius of Earth at given latitude
            val radius = wgs84EarthRadius(lat)
            // Radius of the parallel at given latitude
            val pradius = radius * cos(lat)

            val latMin = lat - halfSide / radius
            val latMax = lat + halfSide / radius
            val lonMin = lon - halfSide / pradius
            val lonMax = lon + halfSide / pradius

            return BoundingBox(
                minPoint = MapPoint(latitude = rad2deg(latMin), longitude = rad2deg(lonMin)),
                maxPoint = MapPoint(latitude = rad2deg(latMax), longitude = rad2deg(lonMax))
            )
        }

        // degrees to radians
        private fun deg2rad(degrees: Double): Double {
            return Math.PI * degrees / 180.0
        }

        // radians to degrees
        private fun rad2deg(radians: Double): Double {
            return 180.0 * radians / Math.PI
        }

        // Earth radius at a given latitude, according to the WGS-84 ellipsoid [m]
        private fun wgs84EarthRadius(lat: Double): Double {
            val an = WGS84_A * WGS84_A * cos(lat)
            val bn = WGS84_B * WGS84_B * sin(lat)
            val ad = WGS84_A * cos(lat)
            val bd = WGS84_B * sin(lat)
            return sqrt((an * an + bn * bn) / (ad * ad + bd * bd))
        }
    }
}