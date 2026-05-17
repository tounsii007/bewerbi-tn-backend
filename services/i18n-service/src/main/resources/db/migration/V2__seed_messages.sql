-- ============================================================
-- Seed i18n bundle: strings used by server-rendered outputs
-- (profile completeness labels, email templates, error keys, …).
-- Client-facing strings can still live in the mobile bundle; this
-- table is the *backend* source of truth so notifications/emails
-- render in the user's locale.
-- ============================================================

-- Tier labels
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','tier.starter','Starter'),
  ('de','default','tier.mover','Aufsteiger'),
  ('de','default','tier.advanced','Fortgeschritten'),
  ('de','default','tier.complete','Komplett'),
  ('fr','default','tier.starter','Débutant'),
  ('fr','default','tier.mover','Progression'),
  ('fr','default','tier.advanced','Avancé'),
  ('fr','default','tier.complete','Complet'),
  ('ar','default','tier.starter','مبتدئ'),
  ('ar','default','tier.mover','في تقدم'),
  ('ar','default','tier.advanced','متقدم'),
  ('ar','default','tier.complete','مكتمل');

-- Profile field labels (referenced by ProfileCompleteness.compute)
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','profile.firstName','Vorname'),
  ('de','default','profile.lastName','Nachname'),
  ('de','default','profile.phone','Telefon'),
  ('de','default','profile.city','Stadt'),
  ('de','default','profile.country','Land'),
  ('de','default','profile.bio','Über mich'),
  ('de','default','profile.photo','Profilfoto'),
  ('de','default','profile.level','Deutsch-Niveau'),
  ('fr','default','profile.firstName','Prénom'),
  ('fr','default','profile.lastName','Nom'),
  ('fr','default','profile.phone','Téléphone'),
  ('fr','default','profile.city','Ville'),
  ('fr','default','profile.country','Pays'),
  ('fr','default','profile.bio','À propos'),
  ('fr','default','profile.photo','Photo de profil'),
  ('fr','default','profile.level','Niveau d''allemand'),
  ('ar','default','profile.firstName','الاسم'),
  ('ar','default','profile.lastName','اللقب'),
  ('ar','default','profile.phone','الهاتف'),
  ('ar','default','profile.city','المدينة'),
  ('ar','default','profile.country','البلد'),
  ('ar','default','profile.bio','نبذة'),
  ('ar','default','profile.photo','الصورة الشخصية'),
  ('ar','default','profile.level','مستوى الألمانية');

-- Profile next-action prompts
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','profileAction.addFirstName','Vornamen ergänzen'),
  ('de','default','profileAction.addLastName','Nachnamen ergänzen'),
  ('de','default','profileAction.addPhone','Telefonnummer hinzufügen'),
  ('de','default','profileAction.addCity','Stadt angeben'),
  ('de','default','profileAction.addCountry','Land angeben'),
  ('de','default','profileAction.addBio','Kurze Vorstellung schreiben'),
  ('de','default','profileAction.addPhoto','Profilfoto hochladen'),
  ('de','default','profileAction.addProfession','Wunschberuf angeben'),
  ('de','default','profileAction.addGerman','Deutsch-Niveau eintragen'),
  ('de','default','profileAction.addRecognition','Anerkennung starten'),
  ('de','default','profileAction.addSkills','Kompetenzen hinzufügen'),
  ('fr','default','profileAction.addFirstName','Compléter le prénom'),
  ('fr','default','profileAction.addLastName','Compléter le nom'),
  ('fr','default','profileAction.addPhone','Ajouter un téléphone'),
  ('fr','default','profileAction.addCity','Renseigner la ville'),
  ('fr','default','profileAction.addCountry','Renseigner le pays'),
  ('fr','default','profileAction.addBio','Écrire une courte présentation'),
  ('fr','default','profileAction.addPhoto','Ajouter une photo'),
  ('fr','default','profileAction.addProfession','Indiquer le métier visé'),
  ('fr','default','profileAction.addGerman','Indiquer votre niveau d''allemand'),
  ('fr','default','profileAction.addRecognition','Démarrer la reconnaissance'),
  ('fr','default','profileAction.addSkills','Ajouter des compétences'),
  ('ar','default','profileAction.addFirstName','أضف اسمك'),
  ('ar','default','profileAction.addLastName','أضف لقبك'),
  ('ar','default','profileAction.addPhone','أضف رقم الهاتف'),
  ('ar','default','profileAction.addCity','حدد المدينة'),
  ('ar','default','profileAction.addCountry','حدد البلد'),
  ('ar','default','profileAction.addBio','اكتب نبذة عنك'),
  ('ar','default','profileAction.addPhoto','أضف صورة شخصية'),
  ('ar','default','profileAction.addProfession','أضف المهنة المرغوبة'),
  ('ar','default','profileAction.addGerman','أضف مستوى الألمانية'),
  ('ar','default','profileAction.addRecognition','ابدأ الاعتراف'),
  ('ar','default','profileAction.addSkills','أضف مهاراتك');

-- Onboarding titles
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','onboarding.step1Title','Welchen Beruf möchtest du ausüben?'),
  ('de','default','onboarding.step4Title','Deine wichtigsten Kompetenzen'),
  ('fr','default','onboarding.step1Title','Quel métier souhaitez-vous exercer ?'),
  ('fr','default','onboarding.step4Title','Vos compétences clés'),
  ('ar','default','onboarding.step1Title','ما هي المهنة التي تود ممارستها؟'),
  ('ar','default','onboarding.step4Title','مهاراتك الأساسية');

-- Anerkennung title
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','anerkennung.title','Anerkennung'),
  ('fr','default','anerkennung.title','Reconnaissance'),
  ('ar','default','anerkennung.title','الاعتراف بالشهادة');

-- Error keys
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','error.auth.email.exists','Die E-Mail-Adresse ist bereits registriert.'),
  ('de','default','error.auth.missing','Nicht angemeldet.'),
  ('de','default','error.validation.failed','Die Eingaben sind unvollständig.'),
  ('de','default','error.internal','Es ist ein unerwarteter Fehler aufgetreten.'),
  ('fr','default','error.auth.email.exists','Cet e-mail est déjà enregistré.'),
  ('fr','default','error.auth.missing','Non connecté.'),
  ('fr','default','error.validation.failed','Les données saisies sont incomplètes.'),
  ('fr','default','error.internal','Une erreur inattendue est survenue.'),
  ('ar','default','error.auth.email.exists','هذا البريد الإلكتروني مسجل مسبقاً.'),
  ('ar','default','error.auth.missing','أنت غير مسجل الدخول.'),
  ('ar','default','error.validation.failed','البيانات المدخلة ناقصة.'),
  ('ar','default','error.internal','حدث خطأ غير متوقع.');

-- Email templates (notification-service)
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','email','verify.subject','Willkommen bei bewerbi.tn — E-Mail bestätigen'),
  ('de','email','verify.body','Hallo {0}, bitte bestätige deine E-Mail mit diesem Link: {1}'),
  ('de','email','newMatches.subject','{0} neue passende Stellen für dich'),
  ('fr','email','verify.subject','Bienvenue sur bewerbi.tn — confirmez votre e-mail'),
  ('fr','email','verify.body','Bonjour {0}, confirmez votre e-mail via ce lien : {1}'),
  ('fr','email','newMatches.subject','{0} nouvelles offres correspondent à votre profil'),
  ('ar','email','verify.subject','مرحباً بك في bewerbi.tn — تأكيد البريد الإلكتروني'),
  ('ar','email','verify.body','مرحباً {0}، يرجى تأكيد بريدك الإلكتروني عبر الرابط التالي: {1}'),
  ('ar','email','newMatches.subject','{0} عروض جديدة تناسب ملفك');
