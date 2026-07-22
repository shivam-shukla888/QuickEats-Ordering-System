// IPGeolocation.io & High-Accuracy HTML5 Device GPS Configuration
export const IPGEOLOCATION_API_KEY = "7b880e23c6614b0faced48654f72a18a";

// 1. High-Accuracy Device GPS Hardware Location (Street & Building Precision)
export const fetchAccurateDeviceGpsLocation = () => {
  return new Promise((resolve) => {
    if (!('geolocation' in navigator)) {
      console.warn('HTML5 Geolocation not supported by browser. Using IP fallback.');
      resolve(fetchRealTimeIpLocation());
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;

        try {
          // Reverse geocode exact lat/lng to street address via OpenStreetMap Nominatim
          const res = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`,
            {
              headers: {
                'User-Agent': 'QuickEats-Portfolio-Project/1.0 (Contact: localdev@quickeats.app)'
              }
            }
          );
          if (res.ok) {
            const data = await res.json();
            resolve({
              address: data.display_name || `GPS Lat: ${lat.toFixed(4)}, Lng: ${lng.toFixed(4)}`,
              city: data.address?.city || data.address?.town || data.address?.suburb || 'Local Area',
              state: data.address?.state || 'Local State',
              zipcode: data.address?.postcode || '',
              lat,
              lng,
              source: 'Device Hardware GPS (High Accuracy)'
            });
            return;
          }
        } catch (e) {
          console.warn('Reverse geocoding failed, returning raw coords:', e);
        }

        resolve({
          address: `Precise Device Location (${lat.toFixed(4)}, ${lng.toFixed(4)})`,
          lat,
          lng,
          source: 'Device Hardware GPS'
        });
      },
      async (error) => {
        console.warn('High-accuracy device GPS denied or timed out. Falling back to IP Location:', error.message);
        const ipLocation = await fetchRealTimeIpLocation();
        resolve(ipLocation);
      },
      {
        enableHighAccuracy: true, // Uses real GPS chip / Wi-Fi triangulation for maximum accuracy
        timeout: 8000,
        maximumAge: 0
      }
    );
  });
};

// 2. IP-Based Location Fallback (City Level)
export const fetchRealTimeIpLocation = async () => {
  try {
    const response = await fetch(`https://api.ipgeolocation.io/ipgeo?apiKey=${IPGEOLOCATION_API_KEY}`);
    if (!response.ok) {
      throw new Error(`IPGeolocation API error status: ${response.status}`);
    }
    const data = await response.json();

    const formattedAddress = [
      data.city,
      data.district,
      data.state_prov,
      data.country_name,
      data.zipcode
    ].filter(Boolean).join(', ');

    return {
      address: formattedAddress || 'IP Geolocation Area',
      city: data.city || 'Delhi',
      state: data.state_prov || 'Delhi',
      district: data.district || '',
      zipcode: data.zipcode || '',
      lat: parseFloat(data.latitude) || 28.6245,
      lng: parseFloat(data.longitude) || 77.2140,
      ip: data.ip || '',
      source: 'IP Geolocation API'
    };
  } catch (err) {
    console.warn('Real-time IPGeolocation API fetch failed:', err);
    return null;
  }
};
