# Screen 10 — SMS Inbox

**Flow:** Basic phone mode (during active session)
**Purpose:** View text message conversations.

---

## Content

### Header

| Element | Copy |
|---------|------|
| **Screen title** | Messages |
| **Back button** | _(Icon only — back arrow.)_ |
| **New message button** | _(Icon only — compose/pencil icon.)_ |

### Conversation List

Each row shows:

| Element | Format |
|---------|--------|
| **Contact name** | [Name] or [Phone number] if not in contacts |
| **Last message preview** | First line of last message, truncated |
| **Timestamp** | Relative: "Now" / "2m" / "1h" / "Yesterday" / "Mon" / "Apr 3" |
| **Unread indicator** | _(Visual dot only. No unread count badge.)_ |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **No messages on device** | No messages yet. |
| **SMS permission not granted** | Wane needs access to your messages to show them here. |
| **SMS permission — how to fix** | Open Settings → Wane → SMS |
| **Message failed to load** | Couldn't load messages. |

---

## Permission Request (if not yet granted)

| Element | Copy |
|---------|------|
| **Explanation** | Wane needs access to your messages so you can read and send texts during your water. |
| **If denied** | You can grant access later in your phone's Settings. |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Screen** | "Messages. Your text message conversations." |
| **Conversation row** | "[Name]. Last message: [preview]. [Timestamp]." |
| **Unread conversation** | "[Name]. Unread. Last message: [preview]. [Timestamp]." |
| **New message button** | "New message. Start a new text conversation." |
| **Empty state** | "No messages yet." |

---

## Notes
- "Messages" not "SMS Inbox" — use the word people actually say.
- No unread count badge (e.g., "3 unread"). The dot indicator is sufficient. Numbers create urgency.
- Timestamps are relative and vague. "2m" not "2 minutes ago." Brevity over precision.
- The conversation list shows the device's existing SMS threads. No filtering or hiding.
