const GCJ_A = 6378245.0;
const GCJ_EE = 0.006693421622965943;

function isOutOfChina(latitude: number, longitude: number) {
  return longitude < 72.004 || longitude > 137.8347 || latitude < 0.8293 || latitude > 55.8271;
}

function transformLatitude(x: number, y: number) {
  let result = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
  result += ((20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0) / 3.0;
  result += ((20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin((y / 3.0) * Math.PI)) * 2.0) / 3.0;
  result += ((160.0 * Math.sin((y / 12.0) * Math.PI) + 320.0 * Math.sin((y * Math.PI) / 30.0)) * 2.0) / 3.0;
  return result;
}

function transformLongitude(x: number, y: number) {
  let result = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
  result += ((20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0) / 3.0;
  result += ((20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin((x / 3.0) * Math.PI)) * 2.0) / 3.0;
  result += ((150.0 * Math.sin((x / 12.0) * Math.PI) + 300.0 * Math.sin((x / 30.0) * Math.PI)) * 2.0) / 3.0;
  return result;
}

export function wgs84ToGcj02(latitude: number, longitude: number): [number, number] {
  if (isOutOfChina(latitude, longitude)) {
    return [latitude, longitude];
  }

  let deltaLatitude = transformLatitude(longitude - 105.0, latitude - 35.0);
  let deltaLongitude = transformLongitude(longitude - 105.0, latitude - 35.0);
  const radLatitude = (latitude / 180.0) * Math.PI;
  let magic = Math.sin(radLatitude);
  magic = 1 - GCJ_EE * magic * magic;
  const sqrtMagic = Math.sqrt(magic);

  deltaLatitude = (deltaLatitude * 180.0) / (((GCJ_A * (1 - GCJ_EE)) / (magic * sqrtMagic)) * Math.PI);
  deltaLongitude = (deltaLongitude * 180.0) / ((GCJ_A / sqrtMagic) * Math.cos(radLatitude) * Math.PI);

  return [latitude + deltaLatitude, longitude + deltaLongitude];
}

export function gcj02ToWgs84(latitude: number, longitude: number): [number, number] {
  if (isOutOfChina(latitude, longitude)) {
    return [latitude, longitude];
  }

  const [gcjLatitude, gcjLongitude] = wgs84ToGcj02(latitude, longitude);
  return [latitude * 2 - gcjLatitude, longitude * 2 - gcjLongitude];
}
