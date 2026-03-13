function formatDateTime(date) {
  return new Intl.DateTimeFormat('en-US', {
    month: 'long', day: 'numeric', year: 'numeric',
    hour: 'numeric', minute: '2-digit', second: '2-digit',
    timeZoneName: 'short'
  }).format(date).replace(' AM', 'am').replace(' PM', 'pm').replace(' at ', ' ');
}

function timeBetween(endISO, offset) {
  const end = new Date(endISO);
  const now = new Date(Date.now() + offset)

  let totalSeconds = Math.floor((end - now) / 1000);
  if (totalSeconds < 0) {
    location.reload();
    return;
  }

  const months = Math.floor(totalSeconds / (30 * 24 * 3600));
  totalSeconds -= months * 30 * 24 * 3600;
  const weeks = Math.floor(totalSeconds / (7 * 24 * 3600));
  totalSeconds -= weeks * 7 * 24 * 3600;
  const days = Math.floor(totalSeconds / (24 * 3600));
  totalSeconds -= days * 24 * 3600;
  const hours = Math.floor(totalSeconds / 3600);
  totalSeconds -= hours * 3600;
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds - minutes * 60;

  const parts = [];
  if (months > 0)  parts.push(months + " month" + (months > 1 ? "s" : ""));
  if (weeks > 0)   parts.push(weeks + " week" + (weeks > 1 ? "s" : ""));
  if (days > 0)    parts.push(days + " day" + (days > 1 ? "s" : ""));
  if (hours > 0)   parts.push(hours + " hour" + (hours > 1 ? "s" : ""));
  if (minutes > 0) parts.push(minutes + " minute" + (minutes > 1 ? "s" : ""));
  if (seconds > 0) parts.push(seconds + " second" + (seconds > 1 ? "s" : ""));

  return parts.join(", ");
}