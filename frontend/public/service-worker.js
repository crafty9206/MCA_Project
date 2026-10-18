self.addEventListener('push', (event) => {
  let message = { title: 'MaatriCare reminder', body: 'Open MaatriCare to view your reminder.', key: '', snoozeToken: '', snoozeUrl: '' }
  try {
    message = { ...message, ...event.data.json() }
  } catch {
    if (event.data) message.body = event.data.text()
  }

  event.waitUntil(self.registration.showNotification(message.title, {
    body: message.body,
    icon: '/favicon.svg',
    badge: '/favicon.svg',
    tag: message.key || 'maatricare-reminder',
    data: { key: message.key, snoozeToken: message.snoozeToken, snoozeUrl: message.snoozeUrl },
    actions: [{ action: 'snooze', title: 'Remind me in 1 hour' }],
  }))
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  const key = event.notification.data?.key
  if (event.action === 'snooze' && key && event.notification.data?.snoozeToken && event.notification.data?.snoozeUrl) {
    event.waitUntil(fetch(event.notification.data.snoozeUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'omit',
      body: JSON.stringify({ deduplicationKey: key, snoozeToken: event.notification.data.snoozeToken }),
    }).then((response) => {
      if (!response.ok) throw new Error('Snooze action rejected')
      return self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windows) => {
        windows.forEach((client) => client.postMessage({ type: 'reminder-snoozed', key }))
      })
    }).catch(() => self.registration.showNotification('MaatriCare reminder', {
      body: 'The reminder could not be snoozed. Open MaatriCare to manage it.',
      icon: '/favicon.svg',
      tag: `snooze-error:${key}`,
    })))
    return
  }
  const target = new URL(self.registration.scope)
  if (key) target.searchParams.set('reminder', key)
  target.hash = 'appointments'
  event.waitUntil(self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windows) => {
    const existing = windows.find((client) => new URL(client.url).origin === target.origin)
    if (existing) return existing.navigate(target.href).then(() => existing.focus())
    return self.clients.openWindow(target.href)
  }))
})