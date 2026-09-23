/**
 * astronomy.js - High Precision Lunar Astronomical Engine
 * Based on Jean Meeus' Astronomical Algorithms.
 * Computes:
 * - Lunar phase, illumination fraction, moon age
 * - Lunar position: Altitude, Azimuth, Parallactic Angle
 * - Horizon events: Moonrise and Moonset times
 * - Lunar events: New Moon, First Quarter, Full Moon, Last Quarter, Perigee, Apogee
 */

const Astronomy = (() => {
  const DEG2RAD = Math.PI / 180.0;
  const RAD2DEG = 180.0 / Math.PI;

  function toJulianDate(date) {
    const time = date.getTime();
    return (time / 86400000.0) + 2440587.5;
  }

  function julianDateToDate(jd) {
    return new Date((jd - 2440587.5) * 86400000.0);
  }

  function normalizeDegrees(deg) {
    let d = deg % 360.0;
    if (d < 0) d += 360.0;
    return d;
  }

  function normalizeRadians(rad) {
    let r = rad % (2 * Math.PI);
    if (r < 0) r += 2 * Math.PI;
    return r;
  }

  // Calculate Sun and Moon positions at given Julian Date
  function getCelestialData(jd) {
    const T = (jd - 2451545.0) / 36525.0; // Julian centuries from J2000.0

    // Sun mean elements
    const L0 = 280.46646 + 36000.76983 * T; // Sun mean longitude
    const M = 357.52911 + 35999.05029 * T; // Sun mean anomaly
    const e = 0.016708634 - 0.000042037 * T; // Earth orbit eccentricity

    const M_rad = normalizeDegrees(M) * DEG2RAD;
    // Sun equation of center
    const C = (1.914602 - 0.004817 * T) * Math.sin(M_rad) +
              (0.019993 - 0.000101 * T) * Math.sin(2 * M_rad) +
              0.000289 * Math.sin(3 * M_rad);
    const sunTrueLong = normalizeDegrees(L0 + C);
    const sunTrueLongRad = sunTrueLong * DEG2RAD;

    // Obliquity of the ecliptic
    const eps0 = 23.43929111 - (46.8150 * T) / 3600.0;
    const epsRad = eps0 * DEG2RAD;

    // Sun right ascension and declination
    const sunRA = Math.atan2(Math.cos(epsRad) * Math.sin(sunTrueLongRad), Math.cos(sunTrueLongRad));
    const sunDec = Math.asin(Math.sin(epsRad) * Math.sin(sunTrueLongRad));

    // Moon fundamental arguments (degrees)
    const Lp = 218.3164477 + 481267.88123421 * T; // Moon mean longitude
    const D = 297.8501921 + 445267.1114034 * T;   // Moon mean elongation
    const M_moon = 134.9633964 + 477198.8675055 * T; // Moon mean anomaly
    const F = 93.2720950 + 483202.0175233 * T;     // Moon argument of latitude

    const D_rad = normalizeDegrees(D) * DEG2RAD;
    const Mm_rad = normalizeDegrees(M_moon) * DEG2RAD;
    const F_rad = normalizeDegrees(F) * DEG2RAD;

    // Periodic terms for Moon's longitude (degrees)
    let lTerm = 6.288774 * Math.sin(Mm_rad)
              + 1.274027 * Math.sin(2 * D_rad - Mm_rad)
              + 0.658314 * Math.sin(2 * D_rad)
              + 0.213618 * Math.sin(2 * Mm_rad)
              - 0.185116 * Math.sin(M_rad)
              - 0.114332 * Math.sin(2 * F_rad)
              + 0.058793 * Math.sin(2 * D_rad - 2 * Mm_rad)
              + 0.057066 * Math.sin(2 * D_rad - M_rad - Mm_rad)
              + 0.053322 * Math.sin(2 * D_rad + Mm_rad)
              + 0.045758 * Math.sin(2 * D_rad - M_rad)
              - 0.040923 * Math.sin(M_rad - Mm_rad)
              - 0.034720 * Math.sin(D_rad)
              - 0.030383 * Math.sin(M_rad + Mm_rad)
              + 0.015327 * Math.sin(2 * D_rad - 2 * F_rad)
              - 0.012528 * Math.sin(Mm_rad + 2 * F_rad);

    const moonEclipticLong = normalizeDegrees(Lp + lTerm);

    // Periodic terms for Moon's latitude (degrees)
    let bTerm = 5.128154 * Math.sin(F_rad)
              + 0.280602 * Math.sin(Mm_rad + F_rad)
              + 0.277693 * Math.sin(Mm_rad - F_rad)
              + 0.173237 * Math.sin(2 * D_rad - F_rad)
              + 0.055413 * Math.sin(2 * D_rad - Mm_rad + F_rad)
              + 0.046271 * Math.sin(2 * D_rad - Mm_rad - F_rad)
              + 0.032573 * Math.sin(2 * D_rad + F_rad)
              + 0.017198 * Math.sin(2 * Mm_rad + F_rad);

    const moonEclipticLat = bTerm;

    // Moon distance (km)
    let distTerm = -20954.0 * Math.cos(Mm_rad)
                 - 3699.0 * Math.cos(2 * D_rad - Mm_rad)
                 - 2956.0 * Math.cos(2 * D_rad)
                 - 570.0 * Math.cos(2 * Mm_rad)
                 + 246.0 * Math.cos(2 * D_rad - 2 * Mm_rad)
                 - 205.0 * Math.cos(M_rad)
                 - 171.0 * Math.cos(Mm_rad + 2 * D_rad)
                 - 152.0 * Math.cos(2 * D_rad - M_rad - Mm_rad);

    const moonDistanceKm = 385000.56 + distTerm;

    // Convert Ecliptic coordinates to Equatorial coordinates
    const lambdaRad = moonEclipticLong * DEG2RAD;
    const betaRad = moonEclipticLat * DEG2RAD;

    const sinDec = Math.sin(betaRad) * Math.cos(epsRad) +
                   Math.cos(betaRad) * Math.sin(epsRad) * Math.sin(lambdaRad);
    const moonDec = Math.asin(sinDec);

    const y = Math.sin(lambdaRad) * Math.cos(epsRad) - Math.tan(betaRad) * Math.sin(epsRad);
    const x = Math.cos(lambdaRad);
    const moonRA = Math.atan2(y, x);

    // Elongation & Phase Angle
    const cosElong = Math.sin(sunDec) * Math.sin(moonDec) +
                     Math.cos(sunDec) * Math.cos(moonDec) * Math.cos(sunRA - moonRA);
    const elongation = Math.acos(Math.max(-1.0, Math.min(1.0, cosElong))); // radians

    // Phase angle i: Sun-Moon-Earth angle in radians
    // At New Moon (elongation ~ 0), i ~ 180 deg (pi rad) -> fraction = 0.0
    // At Full Moon (elongation ~ pi), i ~ 0 deg (0 rad) -> fraction = 1.0
    const phaseAngleRad = Math.PI - elongation;

    // Illumination fraction (0.0 to 1.0)
    const fraction = (1.0 + Math.cos(phaseAngleRad)) / 2.0;

    // Waxing when Moon is east of Sun
    let diffLong = normalizeDegrees(moonEclipticLong - sunTrueLong);

    return {
      jd,
      sunRA,
      sunDec,
      moonRA,
      moonDec,
      moonDistanceKm,
      moonEclipticLong,
      sunTrueLong,
      diffLong, // 0 to 360 degrees (0: New, 90: First Quarter, 180: Full, 270: Last Quarter)
      fraction,
      phaseAngleDeg: diffLong
    };
  }

  // Greenwich Mean Sidereal Time in radians
  function gmst(jd) {
    const T = (jd - 2451545.0) / 36525.0;
    let theta = 280.46061837 + 360.98564736629 * (jd - 2451545.0) +
                0.000387933 * T * T - (T * T * T) / 38710000.0;
    return normalizeDegrees(theta) * DEG2RAD;
  }

  // Calculate Altitude, Azimuth, Parallactic Angle for observer
  function calculateHorizontalPosition(celestial, latDeg, lngDeg) {
    const latRad = latDeg * DEG2RAD;
    const sidereal = gmst(celestial.jd) + lngDeg * DEG2RAD;
    const H = sidereal - celestial.moonRA; // Local Hour Angle

    const sinAlt = Math.sin(latRad) * Math.sin(celestial.moonDec) +
                   Math.cos(latRad) * Math.cos(celestial.moonDec) * Math.cos(H);
    const altRad = Math.asin(Math.max(-1.0, Math.min(1.0, sinAlt)));

    const yAz = -Math.cos(celestial.moonDec) * Math.sin(H);
    const xAz = Math.sin(celestial.moonDec) * Math.cos(latRad) -
                Math.cos(celestial.moonDec) * Math.sin(latRad) * Math.cos(H);
    let azRad = Math.atan2(yAz, xAz);
    if (azRad < 0) azRad += 2 * Math.PI;

    // Parallactic angle: Angle between celestial pole and zenith at moon center
    const denom = Math.tan(latRad) * Math.cos(celestial.moonDec) -
                  Math.sin(celestial.moonDec) * Math.cos(H);
    let parallacticRad = Math.atan2(Math.sin(H), denom);

    return {
      altitudeDeg: altRad * RAD2DEG,
      azimuthDeg: azRad * RAD2DEG,
      parallacticDeg: parallacticRad * RAD2DEG
    };
  }

  // Map phase angle (0-360) to phase enum string matching Android
  function mapAngleToPhase(angle) {
    const norm = normalizeDegrees(angle);
    if (norm < 11.25 || norm >= 348.75) return { key: 'NEW', description: 'New Moon' };
    if (norm < 78.75) return { key: 'WAXING_CRESCENT', description: 'Waxing Crescent' };
    if (norm < 101.25) return { key: 'FIRST_QUARTER', description: 'First Quarter' };
    if (norm < 168.75) return { key: 'WAXING_GIBBOUS', description: 'Waxing Gibbous' };
    if (norm < 191.25) return { key: 'FULL', description: 'Full Moon' };
    if (norm < 258.75) return { key: 'WANING_GIBBOUS', description: 'Waning Gibbous' };
    if (norm < 281.25) return { key: 'LAST_QUARTER', description: 'Last Quarter' };
    return { key: 'WANING_CRESCENT', description: 'Waning Crescent' };
  }

  function calculateMoonAge(phaseAngle) {
    let norm = (phaseAngle) / 360.0;
    while (norm < 0) norm += 1.0;
    while (norm >= 1) norm -= 1.0;
    return norm * 29.530589;
  }

  // Find Moonrise and Moonset for given date and location
  function findHorizonEvents(date, lat, lng) {
    const start = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0);
    const startMs = start.getTime();
    const stepMs = 30 * 60 * 1000;
    const maxSteps = 48;
    const targetAlt = -0.833;

    let riseTime = null;
    let setTime = null;

    let prevJd = toJulianDate(start);
    let prevCel = getCelestialData(prevJd);
    let prevAlt = calculateHorizontalPosition(prevCel, lat, lng).altitudeDeg;

    for (let i = 1; i <= maxSteps && (riseTime === null || setTime === null); i++) {
      const curMs = startMs + i * stepMs;
      const curJd = toJulianDate(new Date(curMs));
      const curCel = getCelestialData(curJd);
      const curAlt = calculateHorizontalPosition(curCel, lat, lng).altitudeDeg;

      if (prevAlt < targetAlt && curAlt >= targetAlt && riseTime === null) {
        riseTime = refineHorizon(startMs + (i - 1) * stepMs, curMs, targetAlt, lat, lng, true);
      } else if (prevAlt > targetAlt && curAlt <= targetAlt && setTime === null) {
        setTime = refineHorizon(startMs + (i - 1) * stepMs, curMs, targetAlt, lat, lng, false);
      }

      prevAlt = curAlt;
    }

    return { riseTime, setTime };
  }

  function refineHorizon(t1, t2, targetAlt, lat, lng, isRising) {
    let low = t1;
    let high = t2;
    for (let i = 0; i < 6; i++) {
      const mid = (low + high) / 2;
      const jd = toJulianDate(new Date(mid));
      const cel = getCelestialData(jd);
      const alt = calculateHorizontalPosition(cel, lat, lng).altitudeDeg;
      if (isRising) {
        if (alt < targetAlt) low = mid; else high = mid;
      } else {
        if (alt > targetAlt) low = mid; else high = mid;
      }
    }
    return new Date((low + high) / 2);
  }

  function checkCrossingAndRefine(p1, p2, t1, t2) {
    let target = null;
    let type = null;

    if (p1 < 90.0 && p2 >= 90.0) { target = 90.0; type = 'FIRST_QUARTER'; }
    else if (p1 < 180.0 && p2 >= 180.0) { target = 180.0; type = 'FULL_MOON'; }
    else if (p1 < 270.0 && p2 >= 270.0) { target = 270.0; type = 'LAST_QUARTER'; }
    else if ((p2 < p1 && p1 > 270.0 && p2 < 90.0) || (p1 > 355.0 && p2 < 5.0)) {
      target = 360.0; type = 'NEW_MOON';
    }

    if (target === null) return null;

    let low = t1;
    let high = t2;
    for (let step = 0; step < 8; step++) {
      const mid = (low + high) / 2;
      let pMid = getCelestialData(toJulianDate(new Date(mid))).diffLong;
      if (target === 360.0 && pMid < 180.0) pMid += 360.0;
      if (pMid < target) low = mid; else high = mid;
    }
    const eventDate = new Date((low + high) / 2);
    const distCel = getCelestialData(toJulianDate(eventDate));
    const isSuper = type === 'FULL_MOON' && distCel.moonDistanceKm < 360000;
    const isMicro = type === 'FULL_MOON' && distCel.moonDistanceKm > 405000;
    return {
      type,
      dateTime: eventDate,
      isSuperMoon: isSuper,
      isMicroMoon: isMicro
    };
  }

  // Find next major lunar phase event from given date
  function findNextMajorEvent(date) {
    let currentMs = date.getTime();
    const stepMs = 4 * 60 * 60 * 1000;
    const maxSteps = (35 * 24) / 4;

    let prevCel = getCelestialData(toJulianDate(new Date(currentMs)));

    for (let i = 0; i < maxSteps; i++) {
      const nextMs = currentMs + stepMs;
      const nextCel = getCelestialData(toJulianDate(new Date(nextMs)));

      const crossedEvent = checkCrossingAndRefine(prevCel.diffLong, nextCel.diffLong, currentMs, nextMs);
      if (crossedEvent) {
        return crossedEvent;
      }

      currentMs = nextMs;
      prevCel = nextCel;
    }

    return null;
  }

  // Find next specific event type (e.g. 'NEW_MOON') from given date
  function findNextEvent(type, fromDate) {
    let currentMs = fromDate.getTime();
    const stepMs = 6 * 60 * 60 * 1000;
    const maxSteps = (45 * 24) / 6;

    let prevCel = getCelestialData(toJulianDate(new Date(currentMs)));

    for (let i = 0; i < maxSteps; i++) {
      const nextMs = currentMs + stepMs;
      const nextCel = getCelestialData(toJulianDate(new Date(nextMs)));

      const crossedEvent = checkCrossingAndRefine(prevCel.diffLong, nextCel.diffLong, currentMs, nextMs);
      if (crossedEvent && crossedEvent.type === type) {
        return crossedEvent;
      }

      currentMs = nextMs;
      prevCel = nextCel;
    }
    return null;
  }

  // Find previous specific event type (e.g. 'NEW_MOON') before given date
  function findPreviousEvent(type, fromDate) {
    let currentMs = fromDate.getTime();
    const stepMs = 6 * 60 * 60 * 1000;
    const maxSteps = (45 * 24) / 6;

    let nextCel = getCelestialData(toJulianDate(new Date(currentMs)));

    for (let i = 0; i < maxSteps; i++) {
      const prevMs = currentMs - stepMs;
      const prevCel = getCelestialData(toJulianDate(new Date(prevMs)));

      const crossedEvent = checkCrossingAndRefine(prevCel.diffLong, nextCel.diffLong, prevMs, currentMs);
      if (crossedEvent && crossedEvent.type === type) {
        return crossedEvent;
      }

      currentMs = prevMs;
      nextCel = prevCel;
    }
    return null;
  }

  // Get all lunar events in a specific time range [startDate, endDate]
  function getLunarEventsInRange(startDate, endDate) {
    const events = [];
    let currentMs = startDate.getTime();
    const endMs = endDate.getTime();
    const stepMs = 6 * 60 * 60 * 1000;

    let prevCel = getCelestialData(toJulianDate(new Date(currentMs)));

    while (currentMs < endMs) {
      const nextMs = currentMs + stepMs;
      const nextCel = getCelestialData(toJulianDate(new Date(nextMs)));

      const crossed = checkCrossingAndRefine(prevCel.diffLong, nextCel.diffLong, currentMs, nextMs);
      if (crossed && crossed.dateTime.getTime() <= endMs && crossed.dateTime.getTime() >= startDate.getTime()) {
        events.push(crossed);
      }

      currentMs = nextMs;
      prevCel = nextCel;
    }

    scanPerigeeApogee(startDate, endDate, events);
    events.sort((a, b) => a.dateTime.getTime() - b.dateTime.getTime());
    return events;
  }

  // Get all lunar events in a specific Gregorian month
  function getLunarEvents(year, month, lat, lng) {
    const startDate = new Date(year, month - 1, 1, 0, 0, 0);
    const endDate = month === 12 ? new Date(year + 1, 0, 1, 0, 0, 0) : new Date(year, month, 1, 0, 0, 0);
    return getLunarEventsInRange(startDate, endDate);
  }

  // Compute full synodic lunar cycle data (from previous New Moon to next New Moon)
  function getLunarCycleData(referenceDate, location) {
    const refDateTime = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      referenceDate.getDate(),
      12, 0, 0
    );

    const prevNewMoon = findPreviousEvent('NEW_MOON', refDateTime);
    const nextNewMoon = findNextEvent('NEW_MOON', refDateTime);

    if (!prevNewMoon || !nextNewMoon) return null;

    const events = getLunarEventsInRange(prevNewMoon.dateTime, nextNewMoon.dateTime);

    // Days in cycle: from 1 day before prevNewMoon date to nextNewMoon date
    // Matches CalendarViewModel.kt:
    // val currentA = prevNewMoon.dateTime.date
    // var current = currentA.minus(1, DateTimeUnit.DAY)
    // val end = nextNewMoon.dateTime.date
    const days = [];
    const dailyMoonData = {};

    const startDay = new Date(
      prevNewMoon.dateTime.getFullYear(),
      prevNewMoon.dateTime.getMonth(),
      prevNewMoon.dateTime.getDate() - 1,
      12, 0, 0
    );
    const endDay = new Date(
      nextNewMoon.dateTime.getFullYear(),
      nextNewMoon.dateTime.getMonth(),
      nextNewMoon.dateTime.getDate(),
      12, 0, 0
    );

    let curr = new Date(startDay.getTime());
    while (curr.getTime() <= endDay.getTime()) {
      const dayCopy = new Date(curr.getTime());
      const dateKey = `${dayCopy.getFullYear()}-${String(dayCopy.getMonth() + 1).padStart(2, '0')}-${String(dayCopy.getDate()).padStart(2, '0')}`;
      days.push(dayCopy);
      dailyMoonData[dateKey] = getMoonData(dayCopy, location, false);
      curr.setDate(curr.getDate() + 1);
    }

    return {
      cycleStart: prevNewMoon,
      cycleEnd: nextNewMoon,
      events,
      days,
      dailyMoonData
    };
  }

  function scanPerigeeApogee(startDate, endDate, events) {
    let currentMs = startDate.getTime();
    const endMs = endDate.getTime();
    const stepMs = 12 * 60 * 60 * 1000;

    let d0 = getCelestialData(toJulianDate(new Date(currentMs - stepMs))).moonDistanceKm;
    let d1 = getCelestialData(toJulianDate(new Date(currentMs))).moonDistanceKm;

    while (currentMs < endMs) {
      const nextMs = currentMs + stepMs;
      const d2 = getCelestialData(toJulianDate(new Date(nextMs))).moonDistanceKm;

      if (d1 < d0 && d1 < d2) {
        const peakDate = refineDistanceExtremum(currentMs - stepMs, nextMs, true);
        if (peakDate >= startDate && peakDate < endDate) {
          events.push({
            type: 'PERIGEE',
            dateTime: peakDate,
            isSuperMoon: false,
            isMicroMoon: false
          });
        }
      } else if (d1 > d0 && d1 > d2) {
        const peakDate = refineDistanceExtremum(currentMs - stepMs, nextMs, false);
        if (peakDate >= startDate && peakDate < endDate) {
          events.push({
            type: 'APOGEE',
            dateTime: peakDate,
            isSuperMoon: false,
            isMicroMoon: false
          });
        }
      }

      d0 = d1;
      d1 = d2;
      currentMs = nextMs;
    }
  }

  function refineDistanceExtremum(t1, t2, isMin) {
    let a = t1;
    let b = t2;
    for (let i = 0; i < 8; i++) {
      const m1 = a + (b - a) / 3;
      const m2 = b - (b - a) / 3;
      const dist1 = getCelestialData(toJulianDate(new Date(m1))).moonDistanceKm;
      const dist2 = getCelestialData(toJulianDate(new Date(m2))).moonDistanceKm;
      if (isMin) {
        if (dist1 < dist2) b = m2; else a = m1;
      } else {
        if (dist1 > dist2) b = m2; else a = m1;
      }
    }
    return new Date((a + b) / 2);
  }

  function getMoonData(date, location, includeDetails = true) {
    const lat = (location && location.latitude !== undefined) ? location.latitude : (location ? location.lat : 0);
    const lng = (location && location.longitude !== undefined) ? location.longitude : (location ? location.lng : 0);
    const jd = toJulianDate(date);
    const celestial = getCelestialData(jd);
    const horizontal = calculateHorizontalPosition(celestial, lat, lng);
    const phaseInfo = mapAngleToPhase(celestial.diffLong);
    const age = calculateMoonAge(celestial.diffLong);

    let horizonTimes = { riseTime: null, setTime: null };
    let nextEvent = null;

    if (includeDetails) {
      horizonTimes = findHorizonEvents(date, lat, lng);
      nextEvent = findNextMajorEvent(date);
    }

    return {
      phase: phaseInfo.key,
      phaseDescription: phaseInfo.description,
      illumination: celestial.fraction,
      age,
      riseTime: horizonTimes.riseTime,
      setTime: horizonTimes.setTime,
      altitude: horizontal.altitudeDeg,
      azimuth: horizontal.azimuthDeg,
      parallacticAngle: horizontal.parallacticDeg,
      nextEvent,
      date
    };
  }

  function formatEventName(event) {
    let base = '';
    switch (event.type) {
      case 'NEW_MOON': base = 'New Moon'; break;
      case 'FIRST_QUARTER': base = 'First Quarter'; break;
      case 'FULL_MOON': base = 'Full Moon'; break;
      case 'LAST_QUARTER': base = 'Last Quarter'; break;
      case 'PERIGEE': base = 'Perigee'; break;
      case 'APOGEE': base = 'Apogee'; break;
      default: base = event.type;
    }
    if (event.isSuperMoon) base += ' (Supermoon)';
    if (event.isMicroMoon) base += ' (Micromoon)';
    return base;
  }

  return {
    getMoonData,
    getLunarEvents,
    getLunarEventsInRange,
    getLunarCycleData,
    findNextEvent,
    findPreviousEvent,
    formatEventName,
    mapAngleToPhase
  };
})();

if (typeof module !== 'undefined' && module.exports) {
  module.exports = Astronomy;
}
