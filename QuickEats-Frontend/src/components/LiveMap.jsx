import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet marker icon asset paths
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom Icons
const createCustomIcon = (emoji, bgColor) => {
  return L.divIcon({
    html: `<div style="background-color: ${bgColor}; width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; box-shadow: 0 4px 10px rgba(0,0,0,0.3); border: 2px solid white;">${emoji}</div>`,
    className: 'custom-leaflet-marker',
    iconSize: [36, 36],
    iconAnchor: [18, 18],
  });
};

const riderIcon = createCustomIcon('🛵', '#ea580c'); // Orange Scooter
const restaurantIcon = createCustomIcon('🍳', '#0f172a'); // Dark Slate Kitchen
const customerIcon = createCustomIcon('🏠', '#16a34a'); // Green House

// Component to dynamically fit bounds covering both restaurant and customer location
const MapFitBounds = ({ restaurantCoords, customerCoords }) => {
  const map = useMap();
  useEffect(() => {
    if (restaurantCoords && customerCoords && restaurantCoords[0] && customerCoords[0]) {
      const bounds = L.latLngBounds([restaurantCoords, customerCoords]);
      map.fitBounds(bounds, { padding: [45, 45], maxZoom: 15 });
    }
  }, [restaurantCoords[0], restaurantCoords[1], customerCoords[0], customerCoords[1]]);
  return null;
};

const LiveMap = ({
  riderLocation,
  restaurantCoords = [28.6328, 77.2197],
  customerCoords = [28.6245, 77.2140],
  customerAddress = 'Current Delivery Address'
}) => {
  const riderPos = [riderLocation?.lat || restaurantCoords[0], riderLocation?.lng || restaurantCoords[1]];
  const [routePolyline, setRoutePolyline] = useState([]);

  useEffect(() => {
    let isMounted = true;

    const fetchOsrmRoute = async () => {
      try {
        const restLat = restaurantCoords[0];
        const restLng = restaurantCoords[1];
        const destLat = customerCoords[0];
        const destLng = customerCoords[1];

        // OSRM format requires: {longitude},{latitude}
        const url = `https://router.project-osrm.org/route/v1/driving/${restLng},${restLat};${destLng},${destLat}?overview=full&geometries=geojson`;
        const res = await fetch(url);
        if (!res.ok) throw new Error('OSRM route fetch failed');
        
        const data = await res.json();
        if (data.routes && data.routes.length > 0 && isMounted) {
          // Convert GeoJSON [lng, lat] coords to Leaflet [lat, lng]
          const coords = data.routes[0].geometry.coordinates.map(coord => [coord[1], coord[0]]);
          setRoutePolyline(coords);
        }
      } catch (err) {
        console.warn('Failed to fetch OSRM route, falling back to straight polyline:', err);
        if (isMounted) {
          setRoutePolyline([restaurantCoords, customerCoords]);
        }
      }
    };

    fetchOsrmRoute();

    return () => { isMounted = false; };
  }, [restaurantCoords[0], restaurantCoords[1], customerCoords[0], customerCoords[1]]);

  return (
    <div className="w-full h-80 rounded-3xl overflow-hidden border border-slate-200 shadow-md relative z-0">
      <MapContainer
        center={customerCoords}
        zoom={14}
        scrollWheelZoom={false}
        className="w-full h-full"
      >
        {/* Free OpenStreetMap Tiles */}
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <MapFitBounds restaurantCoords={restaurantCoords} customerCoords={customerCoords} />

        {/* OSRM Route Line */}
        {routePolyline.length > 0 && (
          <Polyline
            positions={routePolyline}
            pathOptions={{ color: '#ea580c', weight: 5, opacity: 0.7, dashArray: '6, 10' }}
          />
        )}

        {/* 1. Restaurant Marker */}
        <Marker position={restaurantCoords} icon={restaurantIcon}>
          <Popup>
            <div className="text-xs font-bold font-sans">
              <p className="text-orange-600 font-extrabold">Kitchen / Restaurant</p>
              <p>Preparing & Packing Order</p>
            </div>
          </Popup>
        </Marker>

        {/* 2. Customer Delivery Address Marker */}
        <Marker position={customerCoords} icon={customerIcon}>
          <Popup>
            <div className="text-xs font-bold font-sans max-w-xs">
              <p className="text-emerald-600 font-extrabold">Your Delivery Destination</p>
              <p className="truncate">{customerAddress}</p>
            </div>
          </Popup>
        </Marker>

        {/* 3. Moving Rider Marker */}
        <Marker position={riderPos} icon={riderIcon}>
          <Popup>
            <div className="text-xs font-bold font-sans">
              <p className="text-orange-600 font-extrabold">Ramesh Kumar (Rider)</p>
              <p>TVS Jupiter • Live GPS</p>
            </div>
          </Popup>
        </Marker>

      </MapContainer>
    </div>
  );
};

export default LiveMap;
