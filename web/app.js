/**
 * app.js - Main Application Controller
 * Handles tap-first intelligent navigation, synodic lunar cycle calendar,
 * dual-tap reflection journaling, data backup/recovery (JSONL & iCal),
 * 60-second Zen idle timeout, and dynamic favicon updates.
 */

(() => {
  // Preset Cities matching Android App
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
    isEditingNote: false,
    currentReferenceDate: new Date(),
    selectedDate: new Date(),
    currentCycleData: null,
    notes: {}, // Key: YYYY-MM-DD -> { text, calendarDay, dateWritten, lastUpdated }
    mainIdleTimer: null,
    zenIdleTimer: null
  };

  // DOM Elements Cache
  const elements = {
    // Screens & Overlays
    mainScreen: document.getElementById('screen-main'),
    detailsScreen: document.getElementById('screen-details'),
    calendarScreen: document.getElementById('screen-calendar'),
    noteEditorOverlay: document.getElementById('note-editor-overlay'),
    locationModal: document.getElementById('location-modal'),
    statusModal: document.getElementById('status-modal'),

    // Main Screen Elements
    mainMoonContainer: document.getElementById('main-moon-container'),
    mainCanvas: document.getElementById('main-moon-canvas'),
    mainUiOverlay: document.getElementById('main-ui-overlay'),
    touchTargetTopLeft: document.getElementById('touch-target-topleft'),
    btnToggleText: document.getElementById('btn-toggle-text'),
    btnOpenCalendar: document.getElementById('btn-open-calendar'),
    mainPhaseContainer: document.getElementById('main-phase-container'),
    phaseTitle: document.getElementById('phase-title'),
    mainFooterContainer: document.getElementById('main-footer-container'),
    eventCountdown: document.getElementById('event-countdown'),

    // Details Screen Elements
    btnDetailsBack: document.getElementById('btn-details-back'),
    detailCanvas: document.getElementById('detail-moon-canvas'),
    detailPhaseName: document.getElementById('detail-phase-name'),
    detailIlluminationHero: document.getElementById('detail-illumination-hero'),
    btnOpenLocationPicker: document.getElementById('btn-open-location-picker'),
    detailLocationName: document.getElementById('detail-location-name'),
    detailPhaseVal: document.getElementById('detail-phase-val'),
    detailIlluminationVal: document.getElementById('detail-illumination-val'),
    detailAgeVal: document.getElementById('detail-age-val'),
    detailRiseVal: document.getElementById('detail-rise-val'),
    detailSetVal: document.getElementById('detail-set-val'),
    detailAltitudeVal: document.getElementById('detail-altitude-val'),
    detailAzimuthVal: document.getElementById('detail-azimuth-val'),
    btnDownloadJsonl: document.getElementById('btn-download-jsonl'),
    btnUploadJsonl: document.getElementById('btn-upload-jsonl'),
    inputUploadJsonl: document.getElementById('input-upload-jsonl'),
    btnExportIcal: document.getElementById('btn-export-ical'),
    btnImportIcal: document.getElementById('btn-import-ical'),
    inputUploadIcal: document.getElementById('input-upload-ical'),
    toggleWallpaper: document.getElementById('toggle-wallpaper'),
    btnDownloadWallpaper: document.getElementById('btn-download-wallpaper'),

    // Calendar Screen Elements
    btnCalendarBack: document.getElementById('btn-calendar-back'),
    btnPrevCycle: document.getElementById('btn-prev-cycle'),
    btnNextCycle: document.getElementById('btn-next-cycle'),
    cycleLabel: document.getElementById('cycle-label'),
    calendarDaysGrid: document.getElementById('calendar-days-grid'),
    calendarNotePreview: document.getElementById('calendar-note-preview'),
    btnEditNoteDirect: document.getElementById('btn-edit-note-direct'),
    notePreviewText: document.getElementById('note-preview-text'),
    btnCalendarShowDetails: document.getElementById('btn-calendar-show-details'),
    eventsDateTitle: document.getElementById('events-date-title'),
    eventsListContainer: document.getElementById('events-list-container'),

    // Note Editor Elements
    noteEditorHeadline: document.getElementById('note-editor-headline'),
    noteEditorSubtitle: document.getElementById('note-editor-subtitle'),
    btnCloseNote: document.getElementById('btn-close-note'),
    noteTextarea: document.getElementById('note-textarea'),

    // Location Modal Elements
    citySelect: document.getElementById('city-select'),
    btnUseGps: document.getElementById('btn-use-gps'),
    btnModalCancel: document.getElementById('btn-modal-cancel'),

    // Status Modal Elements
    statusModalTitle: document.getElementById('status-modal-title'),
    statusModalMessage: document.getElementById('status-modal-message'),
    btnStatusModalOk: document.getElementById('btn-status-modal-ok')
  };

  // Format Helper: ISO date key YYYY-MM-DD
  function toDateKey(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  function isSameDay(d1, d2) {
    return d1.getFullYear() === d2.getFullYear() &&
           d1.getMonth() === d2.getMonth() &&
           d1.getDate() === d2.getDate();
  }

  // ==========================================
  // NOTE REPOSITORY (localStorage)
  // ==========================================
  function loadNotesFromStorage() {
    try {
      const raw = localStorage.getItem('moon_notes');
      if (raw) {
        state.notes = JSON.parse(raw);
      }
    } catch (e) {
      console.warn('Failed to load notes from localStorage', e);
      state.notes = {};
    }
  }

  function saveNotesToStorage() {
    try {
      localStorage.setItem('moon_notes', JSON.stringify(state.notes));
    } catch (e) {
      console.warn('Failed to save notes to localStorage', e);
    }
  }

  function getNoteEntry(dateKey) {
    return state.notes[dateKey] || null;
  }

  function saveNoteText(dateKey, text) {
    if (!text || text.trim() === '') {
      delete state.notes[dateKey];
    } else {
      const nowIso = new Date().toISOString();
      const existing = state.notes[dateKey];
      state.notes[dateKey] = {
        text: text.trim(),
        calendarDay: dateKey,
        dateWritten: existing ? existing.dateWritten : nowIso,
        lastUpdated: nowIso
      };
    }
    saveNotesToStorage();
  }

  // ==========================================
  // HIGH-DPI CANVAS SCALING
  // ==========================================
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

  // ==========================================
  // ASTRONOMY & MAIN MOON RENDERING
  // ==========================================
  function updateMoon() {
    const now = new Date();
    state.currentMoonData = Astronomy.getMoonData(now, state.location, true);

    // Update Header Text
    elements.phaseTitle.textContent = state.currentMoonData.phaseDescription;

    // Countdown Text (matches Android format)
    if (state.currentMoonData.nextEvent) {
      const event = state.currentMoonData.nextEvent;
      const diffMs = event.dateTime.getTime() - now.getTime();
      const diffDays = Math.floor(diffMs / 86400000);
      const diffHours = Math.floor((diffMs % 86400000) / 3600000);

      const eventName = Astronomy.formatEventName(event);
      let countdown = '';
      if (diffDays > 0) {
        countdown = `${diffDays} ${diffDays === 1 ? 'day' : 'days'} until ${eventName}`;
      } else if (diffHours > 0) {
        countdown = `${diffHours} ${diffHours === 1 ? 'hour' : 'hours'} until ${eventName}`;
      } else {
        countdown = `${eventName} is tonight`;
      }
      elements.eventCountdown.textContent = countdown;
    } else {
      elements.eventCountdown.textContent = '';
    }

    renderMainCanvas();
    updateFavicon();
  }

  function renderMainCanvas() {
    if (!state.currentMoonData) return;
    const { ctx, width, height } = setupCanvasDpi(elements.mainCanvas);
    MoonRenderer.render(ctx, width, height, state.currentMoonData, state.location);
  }

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
      // Ignore favicon render failure in non-browser environments
    }
  }

  // ==========================================
  // ZEN IDLE TIMEOUT & AMBIENT UI FADE
  // ==========================================
  function showUiWithTimer() {
    state.isTextVisible = true;
    elements.mainUiOverlay.classList.remove('hidden-ui');
    clearTimeout(state.mainIdleTimer);
    state.mainIdleTimer = setTimeout(() => {
      if (state.activeScreen === 'main') {
        state.isTextVisible = false;
        elements.mainUiOverlay.classList.add('hidden-ui');
      }
    }, 5000);
  }

  function toggleUi() {
    if (state.isTextVisible) {
      clearTimeout(state.mainIdleTimer);
      state.isTextVisible = false;
      elements.mainUiOverlay.classList.add('hidden-ui');
    } else {
      showUiWithTimer();
    }
  }

  // 60-Second Zen Idle Timer: returns from any screen overlay back to Zen Moon
  function resetZenIdleTimer() {
    clearTimeout(state.zenIdleTimer);
    state.zenIdleTimer = setTimeout(() => {
      if (state.activeScreen !== 'main' || state.isEditingNote) {
        if (state.isEditingNote) {
          closeNoteEditor();
        }
        navigateTo('main');
      }
    }, 60000); // 1 minute inactivity
  }

  // ==========================================
  // NAVIGATION & VIEW SWITCHING
  // ==========================================
  function navigateTo(screenName) {
    state.activeScreen = screenName;
    resetZenIdleTimer();

    elements.mainScreen.classList.remove('active');
    elements.detailsScreen.classList.remove('active');
    elements.calendarScreen.classList.remove('active');

    if (screenName === 'main') {
      elements.mainScreen.classList.add('active');
      renderMainCanvas();
      showUiWithTimer();
    } else if (screenName === 'details') {
      elements.detailsScreen.classList.add('active');
      renderDetailsScreen();
    } else if (screenName === 'calendar') {
      elements.calendarScreen.classList.add('active');
      loadAndRenderSynodicCalendar();
    }
  }

  // ==========================================
  // MOON DETAILS SCREEN
  // ==========================================
  function renderDetailsScreen() {
    if (!state.currentMoonData) updateMoon();
    const data = state.currentMoonData;

    // Render Canvas Hero
    const { ctx, width, height } = setupCanvasDpi(elements.detailCanvas);
    MoonRenderer.render(ctx, width, height, data, state.location);

    elements.detailPhaseName.textContent = data.phaseDescription;
    const illumPercent = Math.round(data.illumination * 100);
    elements.detailIlluminationHero.textContent = `Illumination: ${illumPercent}%`;

    // Location name
    const locText = state.location.name || (state.location.isDefault ? "London" : "Current Location");
    elements.detailLocationName.textContent = locText;

    // Detail Items
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

  // ==========================================
  // TRUE SYNODIC LUNAR CALENDAR
  // ==========================================
  function loadAndRenderSynodicCalendar() {
    state.currentCycleData = Astronomy.getLunarCycleData(state.currentReferenceDate, state.location);
    if (!state.currentCycleData) return;

    const { cycleStart, cycleEnd, days, events, dailyMoonData } = state.currentCycleData;

    // Format Cycle Title: e.g. "Sep 2026" or "Sep - Oct 2026"
    const startMonth = cycleStart.dateTime.toLocaleString('en', { month: 'short' });
    const endMonth = cycleEnd.dateTime.toLocaleString('en', { month: 'short' });
    const year = cycleEnd.dateTime.getFullYear();
    elements.cycleLabel.textContent = startMonth === endMonth
      ? `${startMonth} ${year}`
      : `${startMonth} - ${endMonth} ${year}`;

    // Clear Days Grid
    elements.calendarDaysGrid.innerHTML = '';

    days.forEach(date => {
      const dateKey = toDateKey(date);
      const isSelected = isSameDay(date, state.selectedDate);
      const dayOfWeek = date.getDay(); // 0 = Sunday, 1 = Monday
      const isMonday = (dayOfWeek === 1);
      const isSunday = (dayOfWeek === 0);
      const hasNote = Boolean(state.notes[dateKey] && state.notes[dateKey].text);

      const cell = document.createElement('div');
      let cellClasses = 'day-cell';
      if (isSelected) cellClasses += ' selected';
      else if (isMonday) cellClasses += ' monday';
      else if (isSunday) cellClasses += ' sunday';

      cell.className = cellClasses;
      cell.innerHTML = `
        <span class="day-number">${date.getDate()}</span>
        <canvas class="day-moon-canvas" width="20" height="20"></canvas>
        ${hasNote ? '<span class="day-note-dot" title="Has Note"></span>' : ''}
      `;

      // Render miniature moon
      const dayCanvas = cell.querySelector('canvas');
      const dayData = dailyMoonData[dateKey];
      if (dayCanvas && dayData) {
        const dCtx = dayCanvas.getContext('2d');
        MoonRenderer.render(dCtx, 20, 20, dayData, state.location);
      }

      // Dual-Tap Journaling Logic
      cell.addEventListener('click', (e) => {
        e.stopPropagation();
        resetZenIdleTimer();

        if (isSameDay(date, state.selectedDate)) {
          // Second tap on already selected date -> Open Note Editor!
          openNoteEditor(date);
        } else {
          // First tap -> Select date and update event list & note preview
          state.selectedDate = date;
          updateSelectedDateView();
        }
      });

      elements.calendarDaysGrid.appendChild(cell);
    });

    renderSelectedDateEventsAndNote();
  }

  function updateSelectedDateView() {
    // Re-render grid cell classes
    const days = state.currentCycleData ? state.currentCycleData.days : [];
    const cellElements = elements.calendarDaysGrid.querySelectorAll('.day-cell');

    cellElements.forEach((cell, idx) => {
      if (idx < days.length) {
        const date = days[idx];
        const isSelected = isSameDay(date, state.selectedDate);
        const dayOfWeek = date.getDay();
        const isMonday = (dayOfWeek === 1);
        const isSunday = (dayOfWeek === 0);

        cell.className = 'day-cell';
        if (isSelected) cell.classList.add('selected');
        else if (isMonday) cell.classList.add('monday');
        else if (isSunday) cell.classList.add('sunday');
      }
    });

    renderSelectedDateEventsAndNote();
  }

  function renderSelectedDateEventsAndNote() {
    const d = state.selectedDate;
    const dateKey = toDateKey(d);

    const monthFull = d.toLocaleString('en', { month: 'long' });
    elements.eventsDateTitle.textContent = `Events for ${monthFull} ${d.getDate()}, ${d.getFullYear()}`;

    // Note preview snippet
    const noteEntry = getNoteEntry(dateKey);
    if (noteEntry && noteEntry.text) {
      elements.calendarNotePreview.style.display = 'block';
      elements.notePreviewText.textContent = noteEntry.text;
    } else {
      elements.calendarNotePreview.style.display = 'none';
      elements.notePreviewText.textContent = '';
    }

    // Daily events list
    elements.eventsListContainer.innerHTML = '';
    const dayEvents = (state.currentCycleData ? state.currentCycleData.events : [])
      .filter(e => isSameDay(e.dateTime, d));

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
        canvas.width = 36;
        canvas.height = 36;
        const cCtx = canvas.getContext('2d');
        MoonRenderer.render(cCtx, 36, 36, eventMoonData, state.location);
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

  // Next & Previous Synodic Cycles (Jump by 2 days past cycle boundaries)
  function nextCycle() {
    if (!state.currentCycleData || !state.currentCycleData.cycleEnd) return;
    const currentEnd = state.currentCycleData.cycleEnd.dateTime;
    const nextRef = new Date(currentEnd.getTime() + 2 * 86400000);
    state.currentReferenceDate = nextRef;
    loadAndRenderSynodicCalendar();
  }

  function previousCycle() {
    if (!state.currentCycleData || !state.currentCycleData.cycleStart) return;
    const currentStart = state.currentCycleData.cycleStart.dateTime;
    const prevRef = new Date(currentStart.getTime() - 2 * 86400000);
    state.currentReferenceDate = prevRef;
    loadAndRenderSynodicCalendar();
  }

  // ==========================================
  // FULL-SCREEN DAILY NOTE EDITOR
  // ==========================================
  const IFS_PROMPTS = [
    { title: "New Moon (Invitation)", prompt: "The moon rests in darkness again. I wonder—what Part within me is already awake, quietly waiting to be noticed? Can I, from Self, simply say, 'I see you. There’s no rush, no demand—just space. Would you like to walk this cycle with me?'" },
    { title: "Day 1 (Tending the Spark)", prompt: "The faintest light returns. Is there a Part that feels hesitant to step forward, unsure if it’s safe to be seen? Can I, from Self, offer a quiet invitation: 'You don’t have to be ready. Just being here is enough. I’m listening'?" },
    { title: "Day 2 (Gentle Momentum)", prompt: "The light barely visible. Which Part is eager to push forward, and which one hesitates? can you write from the one that needs to talk most?" },
    { title: "Day 3 (Holding Space)", prompt: "The light is still so soft. Is there a Part that’s been overlooked, not because it’s loud, but because it’s been quiet for so long? Gently say, 'I don’t need you to prove your worth. You belong here, just as you are'?" },
    { title: "Day 4 (Soft Holding)", prompt: "The moon’s glow is still tender. Is there a Part that wants to withdraw, to wait until things feel safer? Can we simply keep the space warm—no pressure, no push—just a quiet 'I’m here' to every part within?" },
    { title: "Day 5 (Whispers of Alignment)", prompt: "The moon carries a little more light tonight. Is there a Part softly tugging my attention—maybe one I’ve overlooked? Can we lean in with kindness and ask, 'What do you need for me to hear you?'" },
    { title: "Day 6 (Bridging Inner Worlds)", prompt: "The light is growing, and so is the pull toward action. Are any Parts starting to align around the intention, while others still linger in doubt? Can I, from Self, welcome both—inviting movement without leaving anyone behind?" },
    { title: "Day 7 (Tending the Spark)", prompt: "The moon is finding its shape, and so am I. Is there a Part that’s starting to believe in this path? Can I, from Self, gently celebrate that flicker of faith—without rushing ahead—just tending the spark with care?" },
    { title: "Waxing Moon (Growth)", prompt: "Which 'Manager' Parts are working hard to achieve my goals right now? Can I acknowledge their effort with Self-compassion?" },
    { title: "Day After Waxing (Balancing Effort)", prompt: "As action builds, am I noticing any Parts taking on too much? Can I, from Self, pause and ask, 'Who needs relief so we can move forward together—not just pushed by the busiest Parts?'" },
    { title: "Mid-Waxing (Inviting Collaboration)", prompt: "The moon grows fuller, and so does the call to act. Are there Parts that have been quiet—maybe creative or playful ones—waiting to contribute? Can I, from Self, gently invite them in, so growth doesn’t just come from effort, but from aliveness?" },
    { title: "Approaching Fullness (Holding Tension)", prompt: "The light is nearly full, and so is my inner world. Are there Parts in conflict—one pushing forward, another pulling back? Can I, from Self, hold the tension without needing to fix it, letting wholeness include both?" },
    { title: "Deepening Light (Welcoming the Edges)", prompt: "The fullness is near, and so is the intensity. Are there Parts I’ve been avoiding—maybe too loud, too raw, or too tender? Can I, from Self, turn toward them not to change them, but to say, 'You belong here too'?" },
    { title: "Near the Peak (Softening the Edges)", prompt: "The light is almost full, and so is the pressure. Is there a Part trying to 'get it right' for everyone? Can I, from Self, gently remind them that wholeness isn’t about perfection—but about presence, even in the wobble?" },
    { title: "Threshold of Fullness (Listening to the Hush)", prompt: "The moon holds its breath before the peak. In the stillness, is there a Part speaking in whispers—maybe one I’ve silenced to stay strong? Can I, from Self, lean in close and let that quiet voice be the one that guides me now?" },
    { title: "The Brightening (Honouring the Build)", prompt: "The moon is almost full, and so is my inner landscape. Are there Parts that have carried quiet burdens to get me here? Can I, from Self, pause and say, 'I see you, and I thank you—for your strength, your silence, your steady hold'?" },
    { title: "The Final Approach (Surrender Before the Peak)", prompt: "One breath from fullness. Is there a Part still trying to control how this unfolds? Can I, from Self, gently invite it to rest—not by force, but by offering, 'I’ve got us. You don’t have to hold on so tightly any more'?" },
    { title: "Full Moon (Release)", prompt: "In this peak energy, am I noticing any 'Firefighter' Parts reacting to intensity? How can I bring Self-presence to soothe the flames?" },
    { title: "The Turn (Softening the Glow)", prompt: "The moon begins its gentle release. Is there a Part that wants to hold on tight to this peak—afraid of what fades with the light? Can I, from Self, offer a steady hand, reminding them that letting go isn’t loss, but trust in the cycle?" },
    { title: "Releasing the Charge (Updating)", prompt: "The moon is turning, and so is the energy. Are there 'Firefighter' Parts still acting from an old threat—holding a picture of danger that’s no longer true? Can I, from Self, gently offer a new image: 'Look around. See the light, feel the breath. We’re not there any more. We’re here'?" },
    { title: "Updating the Inner Map", prompt: "The light continues to soften. Is there a Part still braced for a storm that has already passed—holding onto an old picture of danger? Can I, from Self, gently offer a new view: 'Look around. Feel the stillness. The threat is gone. We’re here, in this quiet, and we’re safe'?" },
    { title: "Curious Inquiry", prompt: "The light is still fading. I wonder—what’s it like for the Parts who’ve been on watch? What are they noticing now, as the intensity softens? And if they’re still holding tension, what world are they seeing—one that’s still stormy, or one that’s already calm?" },
    { title: "Tending the Quiet", prompt: "The moon is less full now, and the energy is turning inward. I wonder—what’s it like for the Parts who’ve been loud or active? Are they winding down on their own, or is there a part of me that’s unsure how to let go of the charge? Can I, from Self, simply ask: 'What do you need to feel safe in this stillness?'" },
    { title: "Listening Beneath the Surface", prompt: "The moon continues to wane, and the inner world grows quieter. I wonder—what’s it like for the Parts who rarely speak up? Are they resting, waiting, or simply feeling unseen? Can I, from Self, gently ask: 'What have you been holding? And what would it feel like to let it be known, just a little?'" },
    { title: "Waning Moon (Reflection)", prompt: "As the light fades, are there any 'Exile' Parts carrying old burdens that are ready to be seen? What does my Self-leadership look like for them tonight?" },
    { title: "Honouring the Hidden", prompt: "The dark is growing, and so is the invitation to listen. Is there a Part that’s been exiled long ago, still holding a story that’s never been told? Can I, from Self, gently ask: 'What do you need for me to finally hear you—not to fix, but to witness?'" },
    { title: "Approaching the Dark (Tender Witnessing)", prompt: "The moon is nearly gone, and the inner world feels hushed. Is there a Part carrying an old wound that’s been hidden, not because it wants to stay buried, but because it’s waited so long to be met with kindness? Can I, from Self, offer not solutions, but soft presence—just saying, 'I’m here. You don’t have to carry this alone any more'?" },
    { title: "Deepening Stillness (Compassionate Holding)", prompt: "The light is almost gone, and the silence grows. Is there a Part that’s been afraid to speak, not because it’s angry, but because it’s tender—afraid of being too much, or not enough? Can I, from Self, simply say: 'You are safe here. Your softness is not weakness. I’ve got you'?" },
    { title: "Threshold of the Dark (Sacred Waiting)", prompt: "We’re nearing the moon’s return to dark. Is there a Part that feels empty, as if something’s missing? Can I, from Self, gently remind it: 'This isn’t loss—it’s preparation. The void isn’t empty; it’s full of what’s waiting to be born'?" },
    { title: "On the Edge of Return (Whispering Gratitude)", prompt: "In this deepest quiet, I wonder—what would it feel like to thank the Parts who’ve carried the weight, even when I didn’t know their names? Can I, from Self, offer a quiet gratitude: 'Thank you for holding on. I see you now. And I’m here to hold you'?" },
    { title: "In the Quiet (Soft Reassurance)", prompt: "The moon is dark now, and the world feels still. Is there a Part that fears this emptiness, as if stillness means absence? Can I, from Self, gently whisper: 'This is not abandonment. This is belonging. You are not alone in the dark—I’m right here with you'?" },
    { title: "Just Before the New (Tending the Embers)", prompt: "The cycle is about to turn. Beneath the silence, is there a Part that’s been waiting—not demanding, just hoping to be seen? Can I, from Self, lean in close and say: 'I know you’ve been here all along. Thank you for your patience. Let's begin again, together'?" }
  ];

  function getIfsPromptForDate(date) {
    if (typeof AstronomyEngine !== 'undefined' && AstronomyEngine.getMoonData) {
      const moonData = AstronomyEngine.getMoonData(date, state.location.lat, state.location.lng);
      if (moonData && typeof moonData.age === 'number') {
        const synodicDays = 29.53059;
        const normalized = ((moonData.age % synodicDays) + synodicDays) % synodicDays / synodicDays;
        const idx = Math.min(Math.floor(normalized * IFS_PROMPTS.length), IFS_PROMPTS.length - 1);
        const p = IFS_PROMPTS[idx];
        return `${p.title}\n"${p.prompt}"`;
      }
    }
    const day = date.getDate();
    const idx = Math.max(0, (day - 1) % IFS_PROMPTS.length);
    const p = IFS_PROMPTS[idx];
    return `${p.title}\n"${p.prompt}"`;
  }

  function openNoteEditor(date) {
    state.isEditingNote = true;
    resetZenIdleTimer();

    const today = new Date();
    const isToday = isSameDay(date, today);
    elements.noteEditorHeadline.textContent = isToday ? "Today's Note" : "Daily Note";

    const monthFull = date.toLocaleString('en', { month: 'long' });
    elements.noteEditorSubtitle.textContent = `${monthFull} ${date.getDate()}, ${date.getFullYear()}`;

    const dateKey = toDateKey(date);
    const existing = getNoteEntry(dateKey);
    elements.noteTextarea.value = existing ? existing.text : '';
    elements.noteTextarea.placeholder = getIfsPromptForDate(date);

    elements.noteEditorOverlay.classList.add('active');
    setTimeout(() => elements.noteTextarea.focus(), 50);
  }

  function closeNoteEditor() {
    state.isEditingNote = false;
    elements.noteEditorOverlay.classList.remove('active');
    loadAndRenderSynodicCalendar();
    resetZenIdleTimer();
  }

  // ==========================================
  // DATA PORTABILITY: JSONL BACKUP & RESTORE
  // ==========================================
  function exportNotesJsonl() {
    const lines = [];
    Object.keys(state.notes).sort().forEach(dateKey => {
      const entry = state.notes[dateKey];
      if (entry && entry.text) {
        lines.push(JSON.stringify({
          text: entry.text,
          calendarDay: entry.calendarDay || dateKey,
          dateWritten: entry.dateWritten || new Date().toISOString(),
          lastUpdated: entry.lastUpdated || new Date().toISOString()
        }));
      }
    });

    const content = lines.join('\n');
    downloadFile(content, 'application/jsonl', 'moon_notes.jsonl');
  }

  function importNotesJsonl(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const text = e.target.result;
        const lines = text.split(/\r?\n/);
        let importedCount = 0;

        lines.forEach(line => {
          const trimmed = line.trim();
          if (!trimmed) return;
          const parsed = JSON.parse(trimmed);
          if (parsed.calendarDay && parsed.text !== undefined) {
            state.notes[parsed.calendarDay] = {
              text: parsed.text,
              calendarDay: parsed.calendarDay,
              dateWritten: parsed.dateWritten || new Date().toISOString(),
              lastUpdated: parsed.lastUpdated || new Date().toISOString()
            };
            importedCount++;
          }
        });

        saveNotesToStorage();
        if (state.activeScreen === 'calendar') loadAndRenderSynodicCalendar();
        showStatusModal('Import Status', `JSONL Data fully loaded! Imported ${importedCount} notes.`);
      } catch (err) {
        showStatusModal('Import Status', 'Error: Invalid JSONL backup file format.');
      }
    };
    reader.readAsText(file);
  }

  // ==========================================
  // CALENDAR SYNC: ICAL (.ICS) EXPORT & IMPORT
  // ==========================================
  function exportIcal() {
    // Generate .ics matching IcsExporter.kt
    const events = (state.currentCycleData ? state.currentCycleData.events : []);
    const lines = [
      'BEGIN:VCALENDAR',
      'VERSION:2.0',
      'PRODID:-//Moon Cycle//Lunar Events//EN',
      'CALSCALE:GREGORIAN',
      'METHOD:PUBLISH'
    ];

    const formatIcsDateTime = (d) => {
      const yr = String(d.getUTCFullYear()).padStart(4, '0');
      const mo = String(d.getUTCMonth() + 1).padStart(2, '0');
      const da = String(d.getUTCDate()).padStart(2, '0');
      const ho = String(d.getUTCHours()).padStart(2, '0');
      const mi = String(d.getUTCMinutes()).padStart(2, '0');
      const se = String(d.getUTCSeconds()).padStart(2, '0');
      return `${yr}${mo}${da}T${ho}${mi}${se}Z`;
    };

    // Export Lunar Events
    events.forEach(evt => {
      lines.push('BEGIN:VEVENT');
      const dtStart = formatIcsDateTime(evt.dateTime);
      const endD = new Date(evt.dateTime.getTime() + 3600000);
      const dtEnd = formatIcsDateTime(endD);
      lines.push(`DTSTART:${dtStart}`);
      lines.push(`DTEND:${dtEnd}`);
      lines.push(`SUMMARY:${Astronomy.formatEventName(evt)}`);
      lines.push('DESCRIPTION:Lunar event calculated by Moon Cycle App');
      lines.push(`GEO:${state.location.latitude};${state.location.longitude}`);
      lines.push('STATUS:CONFIRMED');
      lines.push('TRANSP:OPAQUE');
      lines.push('END:VEVENT');
    });

    // Export Daily Notes as VEVENTs
    Object.keys(state.notes).forEach(dateKey => {
      const entry = state.notes[dateKey];
      if (entry && entry.text) {
        const parts = dateKey.split('-');
        const year = parseInt(parts[0], 10);
        const month = parseInt(parts[1], 10) - 1;
        const day = parseInt(parts[2], 10);
        const dStart = new Date(Date.UTC(year, month, day, 9, 0, 0));
        const dEnd = new Date(Date.UTC(year, month, day, 10, 0, 0));

        lines.push('BEGIN:VEVENT');
        lines.push(`DTSTART:${formatIcsDateTime(dStart)}`);
        lines.push(`DTEND:${formatIcsDateTime(dEnd)}`);
        lines.push('SUMMARY:Lunar Reflection Note');
        lines.push(`DESCRIPTION:${entry.text.replace(/\n/g, '\\n')}`);
        lines.push('STATUS:CONFIRMED');
        lines.push('END:VEVENT');
      }
    });

    lines.push('END:VCALENDAR');
    downloadFile(lines.join('\r\n'), 'text/calendar', 'lunar_notes.ics');
  }

  function importIcal(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const content = e.target.result;
        const vevents = content.split('BEGIN:VEVENT');
        let importedCount = 0;

        for (let i = 1; i < vevents.length; i++) {
          const chunk = vevents[i].split('END:VEVENT')[0];
          let dtStart = '';
          let desc = '';
          let summary = '';

          const lines = chunk.split(/\r?\n/);
          lines.forEach(l => {
            if (l.startsWith('DTSTART:')) dtStart = l.replace('DTSTART:', '').trim();
            else if (l.startsWith('DESCRIPTION:')) desc = l.replace('DESCRIPTION:', '').trim().replace(/\\n/g, '\n');
            else if (l.startsWith('SUMMARY:')) summary = l.replace('SUMMARY:', '').trim();
          });

          if (dtStart && (desc || summary)) {
            // Parse YYYYMMDD
            const y = dtStart.substring(0, 4);
            const m = dtStart.substring(4, 6);
            const d = dtStart.substring(6, 8);
            if (y && m && d) {
              const dateKey = `${y}-${m}-${d}`;
              const noteText = desc && !desc.startsWith('Lunar event') ? desc : summary;
              if (noteText) {
                state.notes[dateKey] = {
                  text: noteText,
                  calendarDay: dateKey,
                  dateWritten: new Date().toISOString(),
                  lastUpdated: new Date().toISOString()
                };
                importedCount++;
              }
            }
          }
        }

        saveNotesToStorage();
        if (state.activeScreen === 'calendar') loadAndRenderSynodicCalendar();
        showStatusModal('Import Status', `iCal Data fully imported! Found ${importedCount} entries.`);
      } catch (err) {
        showStatusModal('Import Status', 'Error: Invalid iCal file format.');
      }
    };
    reader.readAsText(file);
  }

  // ==========================================
  // WALLPAPER GENERATOR
  // ==========================================
  function generateWallpaper() {
    if (!state.currentMoonData) return;
    const offscreen = document.createElement('canvas');
    offscreen.width = 1080;
    offscreen.height = 1920;
    const ctx = offscreen.getContext('2d');

    // Stark black background
    ctx.fillStyle = '#000000';
    ctx.fillRect(0, 0, 1080, 1920);

    // Center Moon
    MoonRenderer.render(ctx, 1080, 1920, state.currentMoonData, state.location);

    const dataUrl = offscreen.toDataURL('image/png');
    const a = document.createElement('a');
    a.href = dataUrl;
    a.download = `moon_wallpaper_${toDateKey(new Date())}.png`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  // Helper to trigger browser file download
  function downloadFile(content, mimeType, fileName) {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  // ==========================================
  // STATUS MODAL DIALOG
  // ==========================================
  function showStatusModal(title, message) {
    elements.statusModalTitle.textContent = title;
    elements.statusModalMessage.textContent = message;
    elements.statusModal.classList.add('active');
  }

  function closeStatusModal() {
    elements.statusModal.classList.remove('active');
  }

  // ==========================================
  // LOCATION PICKER MODAL
  // ==========================================
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
      if (state.activeScreen === 'calendar') loadAndRenderSynodicCalendar();
    }
  }

  function requestGpsLocation() {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          state.location = {
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
            name: "Current Location",
            isDefault: false
          };
          closeLocationModal();
          updateMoon();
          if (state.activeScreen === 'details') renderDetailsScreen();
          if (state.activeScreen === 'calendar') loadAndRenderSynodicCalendar();
        },
        (err) => {
          alert('Could not retrieve device location: ' + err.message);
        },
        { timeout: 15000, enableHighAccuracy: true }
      );
    } else {
      alert('Geolocation is not supported by your browser.');
    }
  }

  // ==========================================
  // EVENT LISTENERS & USER INTERACTIONS
  // ==========================================
  function initEvents() {
    // Global User Interaction Reset for 60s Zen Idle Timer
    ['click', 'touchstart', 'mousemove', 'keydown'].forEach(evt => {
      window.addEventListener(evt, () => resetZenIdleTimer(), { passive: true });
    });

    // Tap-First Navigation on Main Screen
    // 1. Central Moon Canvas tap -> Open Calendar
    elements.mainMoonContainer.addEventListener('click', (e) => {
      e.stopPropagation();
      navigateTo('calendar');
    });

    // 2. Phase Header / Countdown tap -> Open Details
    elements.mainPhaseContainer.addEventListener('click', (e) => {
      e.stopPropagation();
      navigateTo('details');
    });

    elements.mainFooterContainer.addEventListener('click', (e) => {
      e.stopPropagation();
      navigateTo('details');
    });

    // 3. Top-left 64px discrete target -> Toggle UI
    elements.touchTargetTopLeft.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleUi();
    });

    elements.btnToggleText.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleUi();
    });

    elements.btnOpenCalendar.addEventListener('click', (e) => {
      e.stopPropagation();
      navigateTo('calendar');
    });

    // Main screen ambient UI show on blank canvas area click
    elements.mainScreen.addEventListener('click', (e) => {
      if (e.target.closest('button') || e.target.closest('.corner-touch-target')) return;
      showUiWithTimer();
    });

    // Details Screen Actions
    elements.btnDetailsBack.addEventListener('click', () => navigateTo('main'));
    elements.btnOpenLocationPicker.addEventListener('click', openLocationModal);

    elements.btnDownloadJsonl.addEventListener('click', exportNotesJsonl);
    elements.btnUploadJsonl.addEventListener('click', () => elements.inputUploadJsonl.click());
    elements.inputUploadJsonl.addEventListener('change', (e) => {
      if (e.target.files && e.target.files[0]) {
        importNotesJsonl(e.target.files[0]);
        e.target.value = '';
      }
    });

    elements.btnExportIcal.addEventListener('click', exportIcal);
    elements.btnImportIcal.addEventListener('click', () => elements.inputUploadIcal.click());
    elements.inputUploadIcal.addEventListener('change', (e) => {
      if (e.target.files && e.target.files[0]) {
        importIcal(e.target.files[0]);
        e.target.value = '';
      }
    });

    elements.btnDownloadWallpaper.addEventListener('click', generateWallpaper);

    // Calendar Screen Actions
    elements.btnCalendarBack.addEventListener('click', () => navigateTo('main'));
    elements.btnPrevCycle.addEventListener('click', previousCycle);
    elements.btnNextCycle.addEventListener('click', nextCycle);
    elements.btnCalendarShowDetails.addEventListener('click', () => navigateTo('details'));
    elements.btnEditNoteDirect.addEventListener('click', () => openNoteEditor(state.selectedDate));

    // Note Editor Actions
    elements.btnCloseNote.addEventListener('click', closeNoteEditor);
    elements.noteTextarea.addEventListener('input', () => {
      resetZenIdleTimer();
      const dateKey = toDateKey(state.selectedDate);
      saveNoteText(dateKey, elements.noteTextarea.value);
    });

    // Location Picker Modal Actions
    elements.citySelect.addEventListener('change', (e) => {
      if (e.target.value) setManualCity(e.target.value);
    });
    elements.btnUseGps.addEventListener('click', requestGpsLocation);
    elements.btnModalCancel.addEventListener('click', closeLocationModal);
    elements.locationModal.addEventListener('click', (e) => {
      if (e.target === elements.locationModal) closeLocationModal();
    });

    // Status Modal Actions
    elements.btnStatusModalOk.addEventListener('click', closeStatusModal);
    elements.statusModal.addEventListener('click', (e) => {
      if (e.target === elements.statusModal) closeStatusModal();
    });

    // Keyboard Shortcuts
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        if (elements.statusModal.classList.contains('active')) {
          closeStatusModal();
        } else if (elements.locationModal.classList.contains('active')) {
          closeLocationModal();
        } else if (state.isEditingNote) {
          closeNoteEditor();
        } else if (state.activeScreen !== 'main') {
          navigateTo('main');
        }
      } else if (!state.isEditingNote) {
        if (e.key === 'c' || e.key === 'C') {
          navigateTo(state.activeScreen === 'calendar' ? 'main' : 'calendar');
        } else if (e.key === 'd' || e.key === 'D') {
          navigateTo(state.activeScreen === 'details' ? 'main' : 'details');
        } else if (e.key === ' ') {
          toggleUi();
        }
      }
    });

    // Resize handling
    window.addEventListener('resize', () => {
      if (state.activeScreen === 'main') renderMainCanvas();
      if (state.activeScreen === 'details') renderDetailsScreen();
    });

    // Real-time clock update (every 60 seconds)
    setInterval(() => {
      if (state.activeScreen === 'main') updateMoon();
    }, 60000);
  }

  // ==========================================
  // APPLICATION BOOTSTRAP
  // ==========================================
  function init() {
    loadNotesFromStorage();
    populateCities();
    initEvents();
    updateMoon();
    resetZenIdleTimer();
    showUiWithTimer();

    // Register Service Worker for PWA
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('./sw.js').catch(() => {});
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
