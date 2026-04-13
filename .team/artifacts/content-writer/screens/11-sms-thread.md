# Screen 11 — SMS Thread

**Flow:** Basic phone mode (during active session)
**Purpose:** Read and send individual text messages.

---

## Content

### Header

| Element | Copy |
|---------|------|
| **Title** | [Contact Name] or [Phone Number] |
| **Back button** | _(Icon only — back arrow. Returns to SMS inbox.)_ |
| **Call shortcut** | _(Icon only — phone handset icon in header.)_ |

### Message Bubbles

| Element | Format |
|---------|--------|
| **Sent messages** | Right-aligned bubble |
| **Received messages** | Left-aligned bubble |
| **Timestamp** | Shown between message groups, not on every message. Relative format. |

### Compose Bar

| Element | Copy |
|---------|------|
| **Input placeholder** | Message |
| **Send button** | _(Icon only — send arrow.)_ |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **Message sending** | _(No "Sending..." indicator. Send instantly or show sent state.)_ |
| **Message failed to send** | Not sent. Tap to retry. |
| **Empty thread (new conversation)** | _(Empty screen with compose bar. No "Start a conversation!" prompt.)_ |
| **Long message truncation** | _(Don't truncate. Show full messages.)_ |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Screen** | "Conversation with [Name]." |
| **Sent message** | "You: [message text]. [Timestamp]." |
| **Received message** | "[Name]: [message text]. [Timestamp]." |
| **Compose field** | "Type a message to [Name]." |
| **Send button** | "Send message." |
| **Call shortcut** | "Call [Name]." |
| **Failed message** | "Message not sent. [message text]. Tap to retry." |

---

## Notes
- The SMS thread is a standard messaging interface. No customization, no flair.
- Timestamps appear between groups of messages, not on every single bubble. Reduces visual noise.
- "Message" as a placeholder — one word. Not "Type a message..." or "What do you want to say?"
- The call shortcut in the header allows quick switch to calling this person.
- Failed sends show a simple retry option. No alarming red error banners.
