# My Measurement Pro — Premium V3 Product Specification

This branch is the implementation track for the premium tailoring workspace shown in the approved visual reference.

## Brand and visual direction
- Use the supplied measuring-tape M logo from `app/src/main/res/drawable-nodpi/my_measurement_logo.png` as the single source of truth. Do not redraw, pixelate, crop, or replace it with a generic letter mark.
- Premium navy/royal-blue and warm-gold palette, clean light surfaces, strong typography, consistent spacing, rounded cards, subtle elevation.
- The interface must be designed for Android phones first and remain responsive on smaller screens.
- Persistent bottom navigation: Home, Records, Tools, Inbox, Profile.

## Main product areas
1. **Dashboard** — greeting, shop identity, saved and draft counts, quick actions for New Measurement, QR Identity, Calculator, Messages, recent records.
2. **Measurement Book** — searchable customer records, Saved/Draft filters, detailed measurement cards, edit/delete, garment type and date.
3. **Measurement editor** — Shalwar Kameez, Shirt, Trouser, Waistcoat; configurable measurement fields; notes; save or draft.
4. **Tailoring calculator** — fabric quantity × fabric rate + stitching + other charges; clear line-item breakdown, editable values, total and reset/save estimate.
5. **QR Identity** — QR payload for shop identity and Tailor ID; share QR image; camera scanning must parse and validate the app's QR format before opening a shop profile. QR is an identity locator, never a password or authentication token.
6. **Inbox** — conversation list, message thread, timestamps, read state, attachment affordance. Real cross-device messaging requires a secure backend and must not be represented as working while offline.
7. **Business Profile** — owner, shop, Tailor ID, contact information, QR preview/share, account settings and sign-out.
8. **Customer mode** — customer registration/login, view only measurements shared with that customer, request/confirm access through authenticated backend rules.

## Engineering requirements
- Keep measurement records isolated by authenticated account; do not trust a Tailor ID or QR payload as proof of identity.
- Preserve existing local data through database migrations where possible.
- Use secure password/authentication handling. Production cross-device login, cloud backup, chat, and access-controlled sharing need a configured backend (Firebase or equivalent) and security rules.
- Keep offline local storage useful, but label offline-only functionality honestly.
- Add tests for measurement calculations, record filtering, QR payload parsing, and database migrations.
- CI must build the debug APK and unsigned AAB; release AAB signing is a separate secure setup using repository secrets, never committed keystore files.

## Delivery gates
- Gate 1: premium UI and navigation implemented, logo integrated, responsive layouts.
- Gate 2: local measurement book, drafts, calculator, QR generation and QR sharing work.
- Gate 3: camera QR scan and validation work.
- Gate 4: backend configured; real authentication, customer sharing, cloud sync and live chat tested.
- Gate 5: CI build succeeds; APK smoke-tested; signed AAB verified before Play Console upload.

## Current status
This file records the approved product direction. It does not claim that all listed features are implemented. Existing `professional-v2` functionality remains the baseline until each V3 gate is built and tested.
