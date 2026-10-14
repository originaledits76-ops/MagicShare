# FistShot - Gesture-Driven Cross-Device Screen Transfer

FistShot is a tactile, privacy-centric Android application that transforms cross-device content sharing into a physical sensation: close your hand into a fist to grab a screenshot into a virtual "Courier Buffer", and reopen your hand in front of a paired device to beam it across local Wi-Fi instantaneously with end-to-end cryptographic integrity.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions have been aligned based on your clarifying answers and architectural requirements:

- **Confirmed: Hand Tracking & Gesture Engine**: CameraX live stream analysis utilizing skin-chrominance and silhouette contraction heuristics (open palm $\rightarrow$ compact fist $\rightarrow$ open palm) running entirely on-device, paired with an instant manual trigger and gesture simulator toggle for accessibility and test bench operations.
- **Confirmed: Peer Discovery & Secure Pairing**: Local Wi-Fi TCP network communication complemented by Android Network Service Discovery (NSD / mDNS) for instant local zero-configuration broadcast, alongside QR code generation/scanning and 6-digit PIN fallback. No cloud accounts, external relays, or centralized servers are used.
- **Confirmed: Screen Capture & Courier Buffer Architecture**: System-wide MediaProjection capture capability with foreground service support, backed by an in-app viewfinder and interactive Courier Buffer. The Courier Buffer retains the captured payload locally until verified release, preventing accidental data loss during network hiccups.
- **Visual Design Standard**: Apple Human Interface Guidelines (HIG) aesthetic with frosted glassmorphism, fluid micro-interactions, dark charcoal depth layering, continuous squircle cards, and tactile state feedback.

---

## 1. Overview & Core Concept

- **What It Does**: Enables intuitive "pick up and drop" screen sharing across Android tablets and smartphones. A user makes a fist in front of their camera to grab their screen into a temporary Courier Buffer, then extends their open hand toward a paired receiver device to transfer the encrypted image over local Wi-Fi.
- **Target Audience / Persona**: Multi-device power users, digital creators, students, and professionals working simultaneously across phone and tablet who want friction-free, account-less, cloud-free local file transfers.
- **Key Value Proposition**: Completely local, zero-friction peer-to-peer transfer that replaces clumsy email drafts, messaging self-chats, and cloud drive sync delays with an instantaneous, physicalized gesture.

---

## 2. User Experience & Visual Design (Apple HIG & Tactile Craft)

### Visual Identity & Theme
- **Aesthetic Direction**: Apple HIG luxury native craft—clean, airy, frosted translucency (`backdrop-blur`), subtle specular lighting, refined typography with tight tracking, and organic tactile spring motions.
- **Color Palette**:
  - Deep Dark Mode Surface: `#0D0E11` (Deep Space) with card containers in `#16181D` and elevated cards in `#1F222A` (never harsh pure `#000000`).
  - Accent Tint: Radiant Cyan / Electric Aqua (`#00E5FF` & `#0A84FF`) representing active beam transfers and gesture confirmation.
  - Success / Cryptographic Verification: Haptic Emerald (`#30D158`).
  - Warning / Buffer Holding State: Amber Pulse (`#FF9F0A`).
  - Destructive / Disconnect: Vivid Coral (`#FF453A`).
- **Typography & Hierarchy**:
  - Headlines: Bold, tight-tracked (`-0.02em`) sans-serif headers with strong optical contrast.
  - Micro-Labels & Badges: Monospaced uppercase pill badges (e.g., `SHA-256 VERIFIED`, `COURIER: HOLDING`, `NSD: BROADCASTING`).
  - Body: Highly legible, generous line-height typography respecting system scaling.

### Key Screens & User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                              FISTSHOT                                  │
│  [Status: PAIRED TO "Pixel Tablet"]      [● Live NSD] [Settings Gear]  │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                    COURIER BUFFER                            │     │
│   │   ┌───────────────────────┐                                  │     │
│   │   │  [Captured Snapshot]  │  "Holding 1 Capture"             │     │
│   │   │  1080x2400 · 1.4 MB   │  SHA-256: 4f8b9e...3a            │     │
│   │   └───────────────────────┘  Status: READY TO RELEASE        │     │
│   │   [ Beam to Pixel Tablet ]   [ Manual Release ]  [ Discard ] │     │
│   └──────────────────────────────────────────────────────────────┘     │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │              GESTURE RADAR & CAMERAX VIEWFINDER              │     │
│   │   ┌──────────────┐   Hand Pose: [ OPEN PALM ]                │     │
│   │   │  CameraX     │   Confidence: 94%                         │     │
│   │   │  Preview     │   Action: Close fist to capture           │     │
│   │   └──────────────┘   [Simulate Fist] [Simulate Open Hand]    │     │
│   └──────────────────────────────────────────────────────────────┘     │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │           PAIRED DEVICES & ZERO-CONFIG DISCOVERY             │     │
│   │   • Pixel Tablet (192.168.1.42:8888)  [ONLINE - TARGET]      │     │
│   │   • Galaxy Tab S9 (Offline)                                  │     │
│   │   [ + Pair New Device via QR / 6-Digit PIN ]                 │     │
│   └──────────────────────────────────────────────────────────────┘     │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                    TRANSFER HISTORY & LOGS                   │     │
│   │   ✓ screen_20261013_2155.png → Pixel Tablet (1.4MB) SHA-OK   │     │
│   │   ↺ Failed transfer to Galaxy Tab [Retry Button]             │     │
│   └──────────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘
```

1. **Dashboard & Courier Buffer Stage**:
   - Prominently showcases the virtual hand state: **Empty** (waiting for capture), **Holding** (glowing amber squircle card with snapshot preview, dimensions, payload size, and cryptographic fingerprint), or **Transferring** (active beam animation with real-time transfer progress bar).
2. **Gesture Radar & Viewfinder**:
   - Compact rounded squircle viewfinder showing front/rear camera feed with real-time HUD overlays: hand bounding region, detected pose state (`IDLE`, `OPEN_HAND`, `FIST_CLOSED`), and visual tactile triggers. Includes manual simulation controls for testing.
3. **Pairing Modal & QR Flow**:
   - Two-way pairing sheet: "Receive & Show QR" (displays high-contrast QR code with local IP, port, and 6-digit numeric pairing code) or "Scan & Connect" (instant camera QR scanner or direct 6-digit manual entry).
4. **History & Trusted Devices Management**:
   - Room-backed list of trusted devices, status indicators (reachable via NSD vs unreachable), recipient selection switch, and transfer history log with SHA-256 integrity tags and one-tap re-transmission.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: On-Device Gesture Recognition Heuristics vs Heavy External ML Models**
  - *Chosen Approach*: CameraX ImageAnalysis stream using real-time luminance thresholding, skin-chroma filtering, and convex hull/contour contraction ratio to reliably detect Open Palm $\leftrightarrow$ Closed Fist transitions at 30fps without multi-gigabyte models or cloud dependencies.
  - *Why*: Immediate zero-latency responsiveness, battery friendly, runs 100% offline, and does not require complex native C++ binaries.
- **Decision 2: Local Wi-Fi TCP + mDNS Discovery**
  - *Chosen Approach*: Raw TCP sockets with structured binary/JSON framing protocol and Android Network Service Discovery (`android.net.nsd.NsdManager`).
  - *Why*: Direct high-speed local throughput (exceeding 20-50 MB/s), instant zero-configuration peer detection across the local subnet, and zero dependence on external signaling servers or internet connectivity.
- **Decision 3: Fail-Safe Courier Buffer with SHA-256 Integrity**
  - *Chosen Approach*: Store captured screenshots in an isolated on-device cache database with SHA-256 cryptographic checksums. If a transfer disconnects midway, the payload remains in the buffer with a prominent "Retry Delivery" affordance.
  - *Why*: Ensures user content is never lost if a recipient moves out of Wi-Fi range or locks their device.
- **Decision 4: Local Room Database for Trusted Devices & Audit Logs**
  - *Chosen Approach*: Android Room database with KSP code generation storing paired device public keys/IPs/ports, transfer metadata, and transmission results.
  - *Why*: User data stays private on-device as mandated, persisting across app relaunches without any cloud account requirement.

---

## 4. Technical Architecture & Data Strategy

### System Component Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            JETPACK COMPOSE UI                               │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────┐  │
│  │   CourierBufferView   │ │   GestureViewfinder   │ │  DevicePairingUI  │  │
│  └───────────▲───────────┘ └───────────▲───────────┘ └─────────▲─────────┘  │
│              │                         │                       │            │
│              └─────────────────────────┼───────────────────────┘            │
│                                        ▼                                    │
│                            MAIN APP VIEWMODEL                               │
│                    (StateFlow / Events / Orchestrator)                      │
└────────────────────────────────────────┬────────────────────────────────────┘
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        ▼                                ▼                                ▼
┌──────────────────┐           ┌──────────────────┐            ┌──────────────────┐
│ GESTURE ENGINE   │           │ CAPTURE SERVICE  │            │ P2P NETWORK MGR  │
│ • CameraX Stream │           │ • MediaProjection│            │ • NSD Broadcast  │
│ • ImageAnalysis  │           │ • Foreground Svc │            │ • TCP Server     │
│ • Fist Detector  │           │ • Bitmaps & PNGs │            │ • TCP Client     │
│ • Pose StateFlow │           │ • Local Storage  │            │ • SHA-256 Hash   │
└──────────────────┘           └──────────────────┘            └──────────────────┘
        │                                │                                │
        └────────────────────────────────┼────────────────────────────────┘
                                         ▼
                             ┌───────────────────────┐
                             │ ROOM REPOSITORY LAYER │
                             │ • Paired Devices DAO  │
                             │ • Transfer History DAO│
                             │ • Courier Buffer Cache│
                             └───────────────────────┘
```

### Peer-to-Peer Protocol & Packet Sequence

```
Device A (Sender)                                            Device B (Receiver)
       │                                                             │
       │  1. mDNS Discovery / QR Handshake (Exchange IP:Port:Pin)   │
       │<═══════════════════════════════════════════════════════════>│
       │                                                             │
       │  [Fist Gesture Detected] -> Capture Screen to Courier       │
       │  [Open Hand Gesture]     -> Trigger Transfer                │
       │                                                             │
       │  2. TCP Connect (SYN -> ACK)                                │
       │────────────────────────────────────────────────────────────>│
       │  3. METADATA: {"filename", "bytes": 1420500, "sha256": ...} │
       │────────────────────────────────────────────────────────────>│
       │  4. READY_TO_RECEIVE ACK                                    │
       │<────────────────────────────────────────────────────────────│
       │  5. STREAM_PAYLOAD (Raw Encrypted Image Chunks)             │
       │────────────────────────────────────────────────────────────>│
       │  6. Receiver verifies SHA-256 == Computed Hash              │
       │  7. TRANSFER_CONFIRMED ACK                                  │
       │<────────────────────────────────────────────────────────────│
       │  [Courier Buffer Cleared]    [Image Saved to Local Media]   │
       ▼                                                             ▼
```

### Room Database Schema
- **`PairedDeviceEntity`**: `id` (UUID), `deviceName` (e.g., "Pixel Tablet"), `ipAddress`, `port`, `pairingPin`, `isCurrentTarget`, `lastSeenTimestamp`, `trustedSince`.
- **`TransferRecordEntity`**: `id` (UUID), `fileName`, `fileSizeBytes`, `sha256Hash`, `targetDeviceName`, `targetIp`, `direction` (`SENT` / `RECEIVED`), `status` (`SUCCESS`, `FAILED`, `PENDING`), `timestamp`, `localFilePath`.
- **`CourierItem`**: In-memory & local file state tracking current payload holding state, thumbnail bitmap, byte size, and checksum.

---

## Plan Verification & Execution Gates
- Build dependencies: Activate CameraX, Room, Coroutines, and OkHttp/Sockets in `build.gradle.kts`.
- Implement zero-permission QR generator and scanning canvas.
- Complete gesture detection engine with real-time pose HUD and interactive simulator.
- Implement reliable TCP server/client socket routines with packet framing and SHA-256 hashing.
- Build the Apple HIG tactile Compose UI with responsive layouts, fluid glass cards, and status banners.
- Verify compilation with `compile_applet`.
