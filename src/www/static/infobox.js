// const serverNow = new Date("$ISO_NOW");
// const isoNextRingString = "$ISO_NEXT_RING_MAYBE_EMPTY";
// const nextRingOverrideString = "$NEXT_RING_OVERRIDE_STRING_MAYBE_EMPTY";

const clientNow = new Date();
const offset = serverNow - clientNow; // difference in ms

const clockElement = document.getElementById('clock');
clockElement.textContent = formatDateTime(new Date(Date.now() + offset));
setInterval(() => {
  clockElement.textContent = formatDateTime(new Date(Date.now() + offset));
}, 1000);

const countdownElement = document.getElementById('countdown');
if (nextRingOverrideString) {
  countdownElement.textContent = nextRingOverrideString;
} else if (isoNextRingString) {
  const targetTime = new Date(isoNextRingString);
  countdownElement.textContent = timeBetween(targetTime, offset);
  setInterval(() => {
    countdownElement.textContent = timeBetween(targetTime, offset);
  }, 1000);
}