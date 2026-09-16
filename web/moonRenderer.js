/**
 * moonRenderer.js - 3D Canvas Moon Visualization
 * Faithful HTML5 Canvas port of MoonVisualization.kt.
 * Renders:
 * - 3D base sphere with dark gradient
 * - Dynamic waxing/waning phase geometry (arcs & elliptical terminator)
 * - 3D spherical light gradient with highlight and shadow tones
 * - Procedural crater surface enhanced along the solar terminator
 * - Ambient rim lighting
 * - Parallactic angle rotation
 * - Southern hemisphere coordinate flipping
 */

const MoonRenderer = (() => {
  const craterSeeds = [
    [0.2, 0.3], [-0.4, 0.1], [0.1, -0.5],
    [-0.2, -0.3], [0.5, 0.4], [-0.6, -0.2],
    [0.3, -0.1], [0.0, 0.6], [-0.3, 0.5],
    [0.4, -0.4], [0.6, 0.1], [-0.1, -0.7],
    [0.0, 0.0], [-0.5, -0.5], [0.7, 0.3]
  ];

  /**
   * Render the moon onto a 2D canvas context.
   * @param {CanvasRenderingContext2D} ctx
   * @param {number} width - Canvas width in CSS or physical pixels
   * @param {number} height - Canvas height
   * @param {Object} moonData - { phase, illumination, parallacticAngle }
   * @param {Object} locationData - { latitude, longitude }
   */
  function render(ctx, width, height, moonData, locationData = { latitude: 51.5074 }) {
    if (!moonData) return;

    const cx = width / 2;
    const cy = height / 2;
    const radius = Math.min(width, height) * 0.4; // 80% of minDimension / 2

    const isSouthern = (locationData && locationData.latitude < 0);
    const tiltAngle = moonData.parallacticAngle || 0;
    const illumination = Math.max(0, Math.min(1, moonData.illumination !== undefined ? moonData.illumination : 0.5));
    const phase = moonData.phase || 'FULL';

    ctx.save();
    ctx.clearRect(0, 0, width, height);

    // Apply orientation transforms around the moon's center
    ctx.translate(cx, cy);
    if (tiltAngle) {
      ctx.rotate(tiltAngle * Math.PI / 180.0);
    }
    if (isSouthern) {
      ctx.scale(-1, 1);
    }
    ctx.translate(-cx, -cy);

    // Clip all drawings to the moon's spherical boundary
    ctx.save();
    ctx.beginPath();
    ctx.arc(cx, cy, radius, 0, Math.PI * 2);
    ctx.clip();

    // 1. Draw base sphere (unlit/dark side)
    const darkGrad = ctx.createRadialGradient(cx, cy, 0, cx, cy, radius);
    darkGrad.addColorStop(0.0, '#2C2C2C');
    darkGrad.addColorStop(0.7, '#151515');
    darkGrad.addColorStop(1.0, '#080808');

    ctx.fillStyle = darkGrad;
    ctx.fillRect(cx - radius, cy - radius, radius * 2, radius * 2);

    // 2. Light brush colors & position
    const isWaxing = phase === 'WAXING_CRESCENT' ||
                     phase === 'FIRST_QUARTER' ||
                     phase === 'WAXING_GIBBOUS' ||
                     (phase === 'NEW' && illumination > 0);

    const lx = isWaxing ? (cx + radius * 0.4) : (cx - radius * 0.4);
    const ly = cy - radius * 0.1;

    const lightGrad = ctx.createRadialGradient(lx, ly, 0, lx, ly, radius * 1.6);
    lightGrad.addColorStop(0.0, '#FFFFFF');
    lightGrad.addColorStop(0.5, '#FFF9C4');
    lightGrad.addColorStop(1.0, '#FBC02D');

    // 3. Draw illuminated region based on phase
    if (phase === 'FULL') {
      ctx.fillStyle = lightGrad;
      ctx.beginPath();
      ctx.arc(cx, cy, radius, 0, Math.PI * 2);
      ctx.fill();
    } else if (phase !== 'NEW') {
      const innerWidth = Math.abs(illumination - 0.5) * 2.0 * radius;

      if (isWaxing) {
        // Right semicircle lit
        ctx.fillStyle = lightGrad;
        ctx.beginPath();
        ctx.arc(cx, cy, radius, -Math.PI / 2, Math.PI / 2, false);
        ctx.closePath();
        ctx.fill();

        // Oval along terminator
        ctx.beginPath();
        ctx.ellipse(cx, cy, Math.max(0.1, innerWidth), radius, 0, 0, Math.PI * 2);
        if (illumination < 0.5) {
          ctx.fillStyle = darkGrad;
          ctx.fill();
        } else {
          ctx.fillStyle = lightGrad;
          ctx.fill();
        }
      } else {
        // Left semicircle lit
        ctx.fillStyle = lightGrad;
        ctx.beginPath();
        ctx.arc(cx, cy, radius, Math.PI / 2, 3 * Math.PI / 2, false);
        ctx.closePath();
        ctx.fill();

        // Oval along terminator
        ctx.beginPath();
        ctx.ellipse(cx, cy, Math.max(0.1, innerWidth), radius, 0, 0, Math.PI * 2);
        if (illumination < 0.5) {
          ctx.fillStyle = darkGrad;
          ctx.fill();
        } else {
          ctx.fillStyle = lightGrad;
          ctx.fill();
        }
      }
    }

    // 4. Draw craters
    drawCraters(ctx, radius, cx, cy, isWaxing, illumination, phase);

    // 5. Ambient rim light
    const rimGrad = ctx.createRadialGradient(cx, cy, radius * 0.92, cx, cy, radius);
    rimGrad.addColorStop(0.0, 'rgba(255, 255, 255, 0)');
    rimGrad.addColorStop(1.0, 'rgba(255, 255, 255, 0.15)');

    ctx.fillStyle = rimGrad;
    ctx.beginPath();
    ctx.arc(cx, cy, radius, 0, Math.PI * 2);
    ctx.fill();

    ctx.restore(); // Restore clip
    ctx.restore(); // Restore transforms
  }

  function drawCraters(ctx, radius, cx, cy, isWaxing, illumination, phase) {
    const craterShadow = 'rgba(0, 0, 0, 0.14)';
    const craterHighlight = 'rgba(255, 255, 255, 0.09)';

    const litEdgeX = isWaxing ? (0.5 - illumination) * 2.0 : (illumination - 0.5) * 2.0;

    craterSeeds.forEach(([dx, dy]) => {
      const x = cx + dx * radius;
      const y = cy + dy * radius;

      let isLit = false;
      if (phase === 'FULL') {
        isLit = true;
      } else if (phase === 'NEW') {
        isLit = false;
      } else if (isWaxing) {
        isLit = dx > litEdgeX;
      } else {
        isLit = dx < litEdgeX;
      }

      if (isLit) {
        const distToEdge = Math.abs(dx - litEdgeX);
        const terminatorMultiplier = Math.max(0.5, Math.min(1.5, 1.0 - distToEdge));

        const cRadius = radius * (0.04 + Math.abs(dx * dy) * 0.08);
        const shadowOffset = 1.5 * terminatorMultiplier;

        // Shadow circle
        ctx.fillStyle = craterShadow;
        ctx.beginPath();
        ctx.arc(x + shadowOffset, y + shadowOffset, cRadius, 0, Math.PI * 2);
        ctx.fill();

        // Highlight circle
        ctx.fillStyle = craterHighlight;
        ctx.beginPath();
        ctx.arc(x - shadowOffset / 2, y - shadowOffset / 2, cRadius * 0.8, 0, Math.PI * 2);
        ctx.fill();
      }
    });
  }

  /**
   * Render an icon-sized moon to a small canvas or data URL (e.g. for calendar cells or favicon).
   */
  function renderToDataUrl(size, moonData, locationData) {
    const offscreen = document.createElement('canvas');
    offscreen.width = size;
    offscreen.height = size;
    const offCtx = offscreen.getContext('2d');
    render(offCtx, size, size, moonData, locationData);
    return offscreen.toDataURL('image/png');
  }

  return {
    render,
    renderToDataUrl
  };
})();

if (typeof module !== 'undefined' && module.exports) {
  module.exports = MoonRenderer;
}
