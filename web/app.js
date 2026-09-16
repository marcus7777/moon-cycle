/**
 * app.js - Main Application Controller
 * Handles state, view navigation, auto-fade idle timer, geolocation,
 * calendar rendering, and dynamic favicon updates.
 */

(() => {
  // Preset Cities from Android App
  const CITIES = [
    { name: "London", lat: 51.5074, lng: -0.1278 },
    { name: "New York", lat: 40.7128, lng: -74.0060 },
    { name: "Tokyo", lat: 35.6762, lng: 139.6503 },
    { name: "Sydney", lat: -33.8688, lng: 151.2093 },
    { name: "Berlin", lat: 52.5200, lng: 13.4050 },
    { name: "Dubai", lat: 25.2048, lng: 55.2708 },
    { name: "Los Angeles", lat: 34.0522, lng: -118.2437 },
    { name: "Paris", lat: 48.8566, lng: 2.3522 },
    { name: "Mumbai", lat: 19.0760, lng: 72.8777 },
    { name: "São Paulo", lat: -23.5505, lng: -46.6333 },
    { name: "Cairo", lat: 30.0444, lng: 31.2357 },
    { name: "Cape Town", lat: -33.9249, lng: 18.4241 },
    { name: "Moscow", lat: 55.7558, lng: 37.6173 },
    { name: "Beijing", lat: 39.9042, lng: 116.4074 },
    { name: "Singapore", lat: 1.3521, lng: 103.8198 },
    { name: "Bangkok", lat: 13.7563, lng: 100.5018 },
    { name: "Mexico City", lat: 19.4326, lng: -99.1332 },
    { name: "Seoul", lat: 37.5665, lng: 126.9780 },
    { name: "Toronto", lat: 43.6532, lng: -79.3832 },
    { name: "Madrid", lat: 40.4168, lng: -3.7038 }
  ];

  // Application State
  const state = {
    location: {
      latitude: 51.5074,
      longitude: -0.1278,
      name: "London",
      isDefault: true
    },
    currentMoonData: null,
    isTextVisible: true,
    activeScreen: 'main', // 'main', 'details', 'calendar'
    calendarYear: new Date().getFullYear(),
    calendarMonth: new Date().getMonth() + 1, // 1-12
    selectedDate: new Date(),
    calendarEvents: [],
    idleTimer: null
  };

  // DOM Elements Cache
  const elements = {
    mainScreen: document.getElementById('screen-main'),
    detailsScreen: document.getElementById('screen-details'),
    calendarScreen: document.getElementById('screen-calendar'),
    mainCanvas: document.getElementById('main-moon-canvas'),
    uiOverlay: document.querySelector('.ui-overlay'),
    locationBadgeText: document.getElementById('location-name'),
    phaseTitle: document.getElementById('phase-title'),
    eventCountdown: document.getElementById('event-countdown'),
    detailsBtn: document.getElementById('btn-details'),
    btnToggleText: document.getElementById('btn-toggle-text'),
    btnOpenCalendar: document.getElementById('btn-open-calendar'),
    btnDetailsBack: document.getElementById('btn-details-back'),
    btnCalendarBack: document.getElementById('btn-calendar-back'),
    locationModal: document.getElementById('location-modal'),
    citySelect: document.getElementById('city-select'),
    btnUseGps: document.getElementById('btn-use-gps'),
    btnModalCancel: document.getElementById('btn-modal-cancel'),
    locationBadge: document.getElementById('location-badge'),
    
    // Details Screen Elements
    detailCanvas: document.getElementById('detail-moon-canvas'),
    detailPhaseName: document.getElementById('detail-phase-name'),
    detailIlluminationHero: document.getElementById('detail-illumination-hero'),
    detailPhaseVal: document.getElementById('detail-phase-val'),
    detailIlluminationVal: document.getElementById('detail-illumination-val'),
    detailAgeVal: document.getElementById('detail-age-val'),
    detailRiseVal: document.getElementById('detail-rise-val'),
    detailSetVal: document.getElementById('detail-set-val'),
    detailAltitudeVal: document.getElementById('detail-altitude-val'),
    detailAzimuthVal: document.getElementById('detail-azimuth-val'),
    
    // Calendar Screen Elements
    monthLabel: document.getElementById('month-label'),
    btnPrevMonth: document.getElementById('btn-prev-month'),
    btnNextMonth: document.getElementById('btn-next-month'),
    calendarDaysGrid: document.getElementById('calendar-days-grid'),
    eventsDateTitle: document.getElementById('events-date-title'),
    eventsListContainer: document.getElementById('events-list-container')
  };

  // High-DPI Canvas Scaling Helper
  function setupCanvasDpi(canvas) {
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    const width = rect.width || canvas.clientWidth || 300;
    const height = rect.height || canvas.clientHeight || 300;

    canvas.width = Math.round(width * dpr);
    canvas.height = Math.round(height * dpr);

    const ctx = canvas.getContext('2d');
    ctx.resetTransform();
    ctx.scale(dpr, dpr);
    return { ctx, width, height };
  }

  // Update Moon Calculations & Render Main View
  function updateMoon() {
    const now = new Date();
    state.currentMoonData = Astronomy.getMoonData(now, state.location, true);

    // Update Text Elements
    elements.locationBadgeText.textContent = state.location.name || (state.location.isDefault ? "London" : "Custom Location");
    elements.phaseTitle.textContent = state.currentMoonData.phaseDescription;
    
    if (elements.detailsBtn) {
      elements.detailsBtn.textContent = state.location.name 
        ? `Details from ${state.location.name}` 
        : "View Details";
    }

    // Countdown Text
    if (state.currentMoonData.nextEvent) {
      const event = state.currentMoonData.nextEvent;
      const diffMs = event.dateTime.getTime() - now.getTime();
      const diffDays = Math.floor(diffMs / (86400000));
      const diffHours = Math.floor((diffMs % 86400000) / 3600000);

      const eventName = Astronomy.formatEventName(event);
      let countdown = `${eventName} soon`;
      if (diffDays > 0) {
        countdown = `${eventName} in ${diffDays} day${diffDays > 1 ? 's' : ''}`;
      } else if (diffHours > 0) {
        countdown = `${eventName} in ${diffHours} hour${diffHours > 1 ? 's' : ''}`;
      }
      elements.eventCountdown.textContent = countdown;
    } else {
      elements.eventCountdown.textContent = '';
    }

    // Render Canvas
    renderMainCanvas();
    updateFavicon();
  }

  function renderMainCanvas() {
    if (!state.currentMoonData) return;
    const { ctx, width, height } = setupCanvasDpi(elements.mainCanvas);
    MoonRenderer.render(ctx, width, height, state.currentMoonData, state.location);
  }

  // Update Favicon dynamically with the current moon phase
  function updateFavicon() {
    if (!state.currentMoonData) return;
    try {
      const dataUrl = MoonRenderer.renderToDataUrl(64, state.currentMoonData, state.location);
      let link = document.querySelector("link[rel~='icon']");
      if (!link) {
        link = document.createElement('link');
        link.rel = 'icon';
        document.head.appendChild(link);
      }
      link.href = dataUrl;
    } catch (e) {
      // Favicon fallback
    }
  }

  // Idle Timer for Ambient UI Auto-Fade
  function showUiWithTimer() {
    state.isTextVisible = true;
    elements.uiOverlay.classList.remove('hidden-ui');
    resetIdleTimer();
  }

  function hideUi() {
    clearTimeout(state.idleTimer);
    state.isTextVisible = false;
    elements.uiOverlay.classList.add('hidden-ui');
  }

  function toggleUi() {
    if (state.isTextVisible) {
      hideUi();
    } else {
      showUiWithTimer();
    }
  }

  function resetIdleTimer() {
    clearTimeout(state.idleTimer);
    state.idleTimer = setTimeout(() => {
      if (state.activeScreen === 'main') {
        hideUi();
      }
    }, 5000);
  }

  // Navigation Screen Management
  function navigateTo(screenName) {
    state.activeScreen = screenName;

    elements.mainScreen.classList.remove('active');
    elements.detailsScreen.classList.remove('active');
    elements.calendarScreen.classList.remove('active');

    if (screenName === 'main') {
      elements.mainScreen.classList.add('active');
      renderMainCanvas();
      showUiWithTimer();
    } else if (screenName === 'details') {
      clearTimeout(state.idleTimer);
      elements.detailsScreen.classList.add('active');
      renderDetailsScreen();
    } else if (screenName === 'calendar') {
      clearTimeout(state.idleTimer);
      elements.calendarScreen.classList.add('active');
      renderCalendarScreen();
    }
  }

  // Render Moon Detail Screen
  function renderDetailsScreen() {
    if (!state.currentMoonData) return;
    const data = state.currentMoonData;

    // Canvas Hero
    const { ctx, width, height } = setupCanvasDpi(elements.detailCanvas);
    MoonRenderer.render(ctx, width, height, data, state.location);

    elements.detailPhaseName.textContent = data.phaseDescription;
    const illumPercent = Math.round(data.illumination * 100);
    elements.detailIlluminationHero.textContent = `Illumination: ${illumPercent}%`;

    elements.detailPhaseVal.textContent = data.phaseDescription;
    elements.detailIlluminationVal.textContent = `${illumPercent}%`;
    elements.detailAgeVal.textContent = `${data.age.toFixed(1)} days`;

    const formatTime = (d) => {
      if (!d) return '--:--';
      const hh = String(d.getHours()).padStart(2, '0');
      const mm = String(d.getMinutes()).padStart(2, '0');
      return `${hh}:${mm}`;
    };

    elements.detailRiseVal.textContent = formatTime(data.riseTime);
    elements.detailSetVal.textContent = formatTime(data.setTime);

    elements.detailAltitudeVal.textContent = data.altitude !== undefined 
      ? `${data.altitude.toFixed(1)}°` 
      : '--';
    elements.detailAzimuthVal.textContent = data.azimuth !== undefined 
      ? `${data.azimuth.toFixed(1)}°` 
      : '--';
  }

  // Render Calendar Screen
  function renderCalendarScreen() {
    const year = state.calendarYear;
    const month = state.calendarMonth; // 1-12
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    elements.monthLabel.textContent = `${monthNames[month - 1]} ${year}`;

    // Get lunar events for month
    state.calendarEvents = Astronomy.getLunarEvents(year, month, state.location.latitude, state.location.longitude);

    // Days Grid
    elements.calendarDaysGrid.innerHTML = '';

    const firstOfMonth = new Date(year, month - 1, 1);
    const daysInMonth = new Date(year, month, 0).getDate();
    const firstDayOfWeek = firstOfMonth.getDay(); // 0 = Sunday

    // Pre-month empty padding cells
    for (let i = 0; i < firstDayOfWeek; i++) {
      const emptyCell = document.createElement('div');
      emptyCell.className = 'day-cell empty';
      elements.calendarDaysGrid.appendChild(emptyCell);
    }

    // Days in current month
    for (let day = 1; day <= daysInMonth; day++) {
      const cellDate = new Date(year, month - 1, day);
      const isSelected = isSameDay(cellDate, state.selectedDate);

      // Moon Data for this day at noon
      const dayNoon = new Date(year, month - 1, day, 12, 0, 0);
      const dayMoonData = Astronomy.getMoonData(dayNoon, state.location, false);

      // Check if event on this day
      const dayEvents = state.calendarEvents.filter(e => isSameDay(e.dateTime, cellDate));

      const cell = document.createElement('div');
      cell.className = `day-cell ${isSelected ? 'selected' : ''}`;
      cell.innerHTML = `
        <span class="day-number">${day}</span>
        <canvas class="day-moon-canvas" width="20" height="20"></canvas>
        ${dayEvents.length > 0 ? '<span class="day-event-dot"></span>' : ''}
      `;

      // Render miniature moon on the cell's canvas
      const dayCanvas = cell.querySelector('day-moon-canvas') || cell.querySelector('canvas');
      if (dayCanvas) {
        const dCtx = dayCanvas.getContext('2d');
        MoonRenderer.render(dCtx, 20, 20, dayMoonData, state.location);
      }

      cell.addEventListener('click', () => {
        state.selectedDate = cellDate;
        // Re-render selection styles
        document.querySelectorAll('.day-cell').forEach(c => c.classList.remove('selected'));
        cell.classList.add('selected');
        renderEventsList();
      });

      elements.calendarDaysGrid.appendChild(cell);
    }

    renderEventsList();
  }

  function isSameDay(d1, d2) {
    return d1.getFullYear() === d2.getFullYear() &&
           d1.getMonth() === d2.getMonth() &&
           d1.getDate() === d2.getDate();
  }

  // Render Events List for Selected Date
  function renderEventsList() {
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    const d = state.selectedDate;
    elements.eventsDateTitle.textContent = `Events for ${monthNames[d.getMonth()]} ${d.getDate()}, ${d.getFullYear()}`;

    const dayEvents = state.calendarEvents.filter(e => isSameDay(e.dateTime, d));
    elements.eventsListContainer.innerHTML = '';

    if (dayEvents.length === 0) {
      const msg = document.createElement('div');
      msg.className = 'no-events-msg';
      msg.textContent = 'No major lunar events';
      elements.eventsListContainer.appendChild(msg);
      return;
    }

    dayEvents.forEach(event => {
      const card = document.createElement('div');
      card.className = 'event-card';

      const timeStr = `${String(event.dateTime.getHours()).padStart(2, '0')}:${String(event.dateTime.getMinutes()).padStart(2, '0')}`;
      const name = Astronomy.formatEventName(event);

      if (event.type !== 'PERIGEE' && event.type !== 'APOGEE') {
        const phaseKey = mapEventToPhaseKey(event.type);
        const eventMoonData = {
          phase: phaseKey,
          illumination: event.type === 'FULL_MOON' ? 1.0 : (event.type === 'NEW_MOON' ? 0.0 : 0.5),
          parallacticAngle: 0
        };

        const canvas = document.createElement('canvas');
        canvas.className = 'event-card-canvas';
        canvas.width = 40;
        canvas.height = 40;
        const cCtx = canvas.getContext('2d');
        MoonRenderer.render(cCtx, 40, 40, eventMoonData, state.location);

        card.appendChild(canvas);
      } else {
        const iconWrap = document.createElement('div');
        iconWrap.className = 'event-icon-wrapper';
        iconWrap.innerHTML = event.type === 'PERIGEE'
          ? `<svg viewBox="0 0 24 24"><path d="M20 12l-1.41-1.41L13 16.17V4h-2v12.17l-5.58-5.59L4 12l8 8 8-8z"/></svg>`
          : `<svg viewBox="0 0 24 24"><path d="M4 12l1.41 1.41L11 7.83V20h2V7.83l5.58 5.59L20 12l-8-8-8 8z"/></svg>`;
        card.appendChild(iconWrap);
      }

      const info = document.createElement('div');
      info.className = 'event-info';
      info.innerHTML = `
        <span class="event-name">${name}</span>
        <span class="event-time">${timeStr}</span>
      `;
      card.appendChild(info);

      elements.eventsListContainer.appendChild(card);
    });
  }

  function mapEventToPhaseKey(type) {
    switch (type) {
      case 'NEW_MOON': return 'NEW';
      case 'FIRST_QUARTER': return 'FIRST_QUARTER';
      case 'FULL_MOON': return 'FULL';
      case 'LAST_QUARTER': return 'LAST_QUARTER';
      default: return 'NEW';
    }
  }

  // Populate City Picker Modal Dropdown
  function populateCities() {
    elements.citySelect.innerHTML = '<option value="" disabled selected>Select a City</option>';
    CITIES.forEach(city => {
      const opt = document.createElement('option');
      opt.value = city.name;
      opt.textContent = city.name;
      elements.citySelect.appendChild(opt);
    });
  }

  function openLocationModal() {
    elements.locationModal.classList.add('active');
  }

  function closeLocationModal() {
    elements.locationModal.classList.remove('active');
  }

  function setManualCity(cityName) {
    const city = CITIES.find(c => c.name === cityName);
    if (city) {
      state.location = {
        latitude: city.lat,
        longitude: city.lng,
        name: city.name,
        isDefault: false
      };
      closeLocationModal();
      updateMoon();
      if (state.activeScreen === 'details') renderDetailsScreen();
      if (state.activeScreen === 'calendar') renderCalendarScreen();
    }
  }

  function requestGpsLocation() {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          state.location = {
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
            name: "My Location",
            isDefault: false
          };
          closeLocationModal();
          updateMoon();
          if (state.activeScreen === 'details') renderDetailsScreen();
          if (state.activeScreen === 'calendar') renderCalendarScreen();
        },
        (err) => {
          alert('Could not retrieve device location: ' + err.message);
        },
        { timeout: 10000, enableHighAccuracy: true }
      );
    } else {
      alert('Geolocation is not supported by your browser.');
    }
  }

  // Swipe Gesture Handling (Horizontal swipe)
  let touchStartX = 0;
  let touchStartY = 0;

  function initSwipeGestures() {
    document.addEventListener('touchstart', (e) => {
      touchStartX = e.changedTouches[0].screenX;
      touchStartY = e.changedTouches[0].screenY;
      if (state.activeScreen === 'main') {
        showUiWithTimer();
      }
    }, { passive: true });

    document.addEventListener('touchend', (e) => {
      const touchEndX = e.changedTouches[0].screenX;
      const touchEndY = e.changedTouches[0].screenY;
      const dx = touchEndX - touchStartX;
      const dy = touchEndY - touchStartY;

      // Ensure horizontal swipe dominates
      if (Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy) * 1.5) {
        if (dx < -60) {
          // Swipe Left -> Open Calendar from Main
          if (state.activeScreen === 'main') {
            navigateTo('calendar');
          }
        } else if (dx > 60) {
          // Swipe Right -> Back to Main
          if (state.activeScreen === 'calendar' || state.activeScreen === 'details') {
            navigateTo('main');
          }
        }
      }
    }, { passive: true });
  }

  // Keyboard Shortcuts
  function initKeyboard() {
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        if (elements.locationModal.classList.contains('active')) {
          closeLocationModal();
        } else if (state.activeScreen !== 'main') {
          navigateTo('main');
        }
      } else if (e.key === 'c' || e.key === 'C') {
        if (state.activeScreen === 'calendar') navigateTo('main');
        else navigateTo('calendar');
      } else if (e.key === 'd' || e.key === 'D') {
        if (state.activeScreen === 'details') navigateTo('main');
        else navigateTo('details');
      } else if (e.key === ' ') {
        toggleUi();
      }
    });
  }

  // Setup Event Listeners
  function initEvents() {
    // Ambient UI tap
    elements.mainScreen.addEventListener('click', (e) => {
      // If clicking interactive controls, don't just toggle
      if (e.target.closest('button') || e.target.closest('.location-badge') || e.target.closest('.modal-card')) {
        return;
      }
      showUiWithTimer();
    });

    elements.btnToggleText.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleUi();
    });

    elements.btnOpenCalendar.addEventListener('click', (e) => {
      e.stopPropagation();
      navigateTo('calendar');
    });

    elements.detailsBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      navigateTo('details');
    });

    elements.btnDetailsBack.addEventListener('click', () => navigateTo('main'));
    elements.btnCalendarBack.addEventListener('click', () => navigateTo('main'));

    // Location Modal
    elements.locationBadge.addEventListener('click', (e) => {
      e.stopPropagation();
      openLocationModal();
    });

    elements.btnModalCancel.addEventListener('click', closeLocationModal);
    elements.locationModal.addEventListener('click', (e) => {
      if (e.target === elements.locationModal) closeLocationModal();
    });

    elements.citySelect.addEventListener('change', (e) => {
      if (e.target.value) setManualCity(e.target.value);
    });

    elements.btnUseGps.addEventListener('click', requestGpsLocation);

    // Calendar Month Navigation
    elements.btnPrevMonth.addEventListener('click', () => {
      state.calendarMonth--;
      if (state.calendarMonth < 1) {
        state.calendarMonth = 12;
        state.calendarYear--;
      }
      renderCalendarScreen();
    });

    elements.btnNextMonth.addEventListener('click', () => {
      state.calendarMonth++;
      if (state.calendarMonth > 12) {
        state.calendarMonth = 1;
        state.calendarYear++;
      }
      renderCalendarScreen();
    });

    // Resize handling
    window.addEventListener('resize', () => {
      if (state.activeScreen === 'main') renderMainCanvas();
      if (state.activeScreen === 'details') renderDetailsScreen();
    });

    // Periodic time check (e.g. every minute to update countdown & real-time angles)
    setInterval(() => {
      if (state.activeScreen === 'main') updateMoon();
    }, 60000);
  }

  // Bootstrap Application
  function init() {
    populateCities();
    initEvents();
    initSwipeGestures();
    initKeyboard();
    updateMoon();
    resetIdleTimer();

    // Register Service Worker for PWA
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('./sw.js').catch(() => {});
    }
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
