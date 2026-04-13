# Screen 16 — Emergency Contacts

**Flow:** Settings → Emergency Contacts
**Purpose:** Safety configuration. Serious but not frightening.

---

## Content

### Header

| Element | Copy |
|---------|------|
| **Screen title** | Emergency contacts |
| **Back button** | _(Icon only — back arrow.)_ |

---

### Explanation

| Element | Copy |
|---------|------|
| **Description (top of screen)** | These people can always call and text you, even during water. |

---

### Contact List

| Element | Format |
|---------|--------|
| **Each contact** | [Name] · [Phone Number] |
| **Remove button** | _(Icon only — X or remove icon per contact.)_ |

### Add Contact

| Element | Copy |
|---------|------|
| **Add button** | Add a contact |
| **Picker** | _(Opens device contact picker.)_ |

---

### Safety Info Section

| Element | Copy |
|---------|------|
| **Section header** | Always available |
| **Emergency services** | Emergency calls (911, 112, 999) always work. |
| **Repeated callers** | If someone calls 3 times in 5 minutes, they ring through. |
| **Android emergency** | Your phone's built-in emergency features are never affected. |

---

## Microcopy

| Scenario | Copy |
|----------|------|
| **No emergency contacts added** | No emergency contacts yet. Add people who should always be able to reach you. |
| **Contact added** | _(No confirmation toast. The contact appears in the list.)_ |
| **Contact removed — confirmation** | Remove [Name]? They won't ring through during your water anymore. |
| **Contact removed — confirm button** | Remove |
| **Contact removed — cancel button** | Keep |
| **Maximum contacts reached (if applicable)** | _(No arbitrary maximum. Let users add as many as they need.)_ |
| **Duplicate contact added** | [Name] is already in your emergency contacts. |
| **Contact permission not granted** | Wane needs access to your contacts to add emergency contacts. |

---

## Permission Request (if not yet granted)

| Element | Copy |
|---------|------|
| **Explanation** | Wane needs access to your contacts to let you choose emergency contacts. |
| **If denied** | You can grant access in your phone's Settings. |

---

## Accessibility

| Element | Screen Reader Text |
|---------|-------------------|
| **Screen** | "Emergency contacts. People who can always reach you during sessions." |
| **Description** | "These people can always call and text you, even during water." |
| **Contact entry** | "[Name]. [Phone Number]. Tap to remove." |
| **Remove button** | "Remove [Name] from emergency contacts." |
| **Add button** | "Add an emergency contact from your contacts." |
| **Emergency services info** | "Emergency calls to 911, 112, and 999 always work." |
| **Repeated callers info** | "If someone calls 3 times in 5 minutes, they ring through automatically." |
| **Empty state** | "No emergency contacts yet. Add people who should always be able to reach you." |

---

## Notes
- The tone is careful but not alarming. "These people can always call and text you" — simple, direct.
- The "Always available" safety section is visible at all times, even before any contacts are added. Users need to see that 911 always works.
- The removal confirmation is factual: "They won't ring through during your water anymore." Not scary, just clear.
- "Keep" instead of "Cancel" for the removal dialog — warmer, and clearer about what the button does.
- No cap on the number of emergency contacts. Safety should not be gated.
