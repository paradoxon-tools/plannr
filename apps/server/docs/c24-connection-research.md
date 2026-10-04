# C24 connection credentials

Researched 2026-10-06.

Enable Banking announced C24 Germany AIS and PIS integration in its [October 2024 update](https://enablebanking.com/blog/2024/11/06/changelog-october-2024). This establishes support, not current availability of a particular connection.

C24's [PSD2 explanation](https://hilfe.c24.de/hc/de/articles/360017014279-Wie-ist-PSD2-bei-der-C24-Bank-umgesetzt) says third-party access uses finAPI and approval in the C24 app, with decoupled authentication.

The official [Lexware C24 connection guide](https://help.lexware.de/de-form/articles/548920-lexware-office-c24-bank-anbinden) explicitly specifies the phone number in the same format stored in the banking app and the banking app PIN; approval uses the C24 app. Mapping Enable Banking's generic username/password fields to phone number/app PIN is an inference from this documented third-party flow. No Enable Banking-specific public credential mapping was found.

C24 documents the app PIN as a four-digit login code in its [security explanation](https://hilfe.c24.de/hc/de/articles/360011421500-Warum-ist-Banking-bei-der-C24-Bank-sicher). Its [PIN change instructions](https://hilfe.c24.de/hc/de/articles/360015710119-Wie-%C3%A4ndere-ich-die-App-PIN) give Profile > Sicherheit & Datenschutz > PIN ändern. The intended credential is the app login PIN, not the card PIN or security password.

Recommended flow: select C24 and personal access, supply registered phone number and app login PIN on the provider authorization page, then approve in C24. If rejected, obtain the exact validation error and ask Enable Banking to confirm the connector's required phone formatting; do not invent or repeatedly guess credentials.
