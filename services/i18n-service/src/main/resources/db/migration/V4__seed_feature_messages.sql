-- ============================================================
-- Keys referenced by immigration-service (step/requirement titles
-- and descriptions are resolved at runtime via MessageClient).
-- ============================================================

-- Anerkennung authorities (inferred by profession)
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','anerkennung.authority.health','Zuständige Landesbehörde (Gesundheitsbereich)'),
  ('de','default','anerkennung.authority.handwerk','Handwerkskammer'),
  ('de','default','anerkennung.authority.ihkfosa','IHK FOSA (Foreign Skills Approval)'),
  ('de','default','anerkennung.authority.zab','Zentralstelle für ausländisches Bildungswesen (ZAB)'),
  ('fr','default','anerkennung.authority.health','Autorité compétente (domaine santé)'),
  ('fr','default','anerkennung.authority.handwerk','Chambre des métiers'),
  ('fr','default','anerkennung.authority.ihkfosa','IHK FOSA (reconnaissance des qualifications)'),
  ('fr','default','anerkennung.authority.zab','Service central pour l''éducation étrangère (ZAB)'),
  ('ar','default','anerkennung.authority.health','السلطة المختصة (قطاع الصحة)'),
  ('ar','default','anerkennung.authority.handwerk','غرفة الحرف'),
  ('ar','default','anerkennung.authority.ihkfosa','IHK FOSA (الاعتراف بالمهارات الأجنبية)'),
  ('ar','default','anerkennung.authority.zab','المركز المركزي للتعليم الأجنبي (ZAB)');

-- Anerkennung step titles + descriptions (6 steps)
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','anerkennung.step.1.title','Informationsgespräch ZAB / IHK FOSA'),
  ('de','default','anerkennung.step.1.desc','Kostenlose Erstberatung zur Anerkennung deines ausländischen Berufsabschlusses.'),
  ('de','default','anerkennung.step.2.title','Unterlagen zusammenstellen'),
  ('de','default','anerkennung.step.2.desc','Originalzeugnisse, beglaubigte deutsche Übersetzungen, Lebenslauf, Identitätsnachweis.'),
  ('de','default','anerkennung.step.3.title','Antrag auf Anerkennung stellen'),
  ('de','default','anerkennung.step.3.desc','Antrag bei der zuständigen Stelle einreichen (IHK FOSA / Handwerkskammer / Landesbehörde).'),
  ('de','default','anerkennung.step.4.title','Gleichwertigkeitsprüfung abwarten'),
  ('de','default','anerkennung.step.4.desc','Die zuständige Stelle prüft innerhalb von ca. 3 Monaten die Gleichwertigkeit.'),
  ('de','default','anerkennung.step.5.title','Ausgleichsmaßnahme (falls nötig)'),
  ('de','default','anerkennung.step.5.desc','Anpassungslehrgang, Kenntnisprüfung oder Eignungstest bei wesentlichen Unterschieden.'),
  ('de','default','anerkennung.step.6.title','Anerkennungsbescheid erhalten'),
  ('de','default','anerkennung.step.6.desc','Volle, teilweise oder keine Gleichwertigkeit — inklusive Begründung.'),

  ('fr','default','anerkennung.step.1.title','Entretien d''information ZAB / IHK FOSA'),
  ('fr','default','anerkennung.step.1.desc','Conseil initial gratuit pour la reconnaissance de votre diplôme étranger.'),
  ('fr','default','anerkennung.step.2.title','Rassemblement des documents'),
  ('fr','default','anerkennung.step.2.desc','Diplômes originaux, traductions certifiées en allemand, CV, pièce d''identité.'),
  ('fr','default','anerkennung.step.3.title','Dépôt de la demande de reconnaissance'),
  ('fr','default','anerkennung.step.3.desc','Demande auprès de l''autorité compétente (IHK FOSA / Chambre des métiers / autorité régionale).'),
  ('fr','default','anerkennung.step.4.title','Attente de l''évaluation d''équivalence'),
  ('fr','default','anerkennung.step.4.desc','L''autorité compétente évalue l''équivalence en environ 3 mois.'),
  ('fr','default','anerkennung.step.5.title','Mesure compensatoire (si nécessaire)'),
  ('fr','default','anerkennung.step.5.desc','Stage d''adaptation, examen d''aptitude ou test en cas de différences significatives.'),
  ('fr','default','anerkennung.step.6.title','Réception de la décision de reconnaissance'),
  ('fr','default','anerkennung.step.6.desc','Équivalence totale, partielle ou refusée — avec motivation.'),

  ('ar','default','anerkennung.step.1.title','جلسة معلومات ZAB / IHK FOSA'),
  ('ar','default','anerkennung.step.1.desc','استشارة أولية مجانية للاعتراف بشهادتك المهنية الأجنبية.'),
  ('ar','default','anerkennung.step.2.title','تجميع الوثائق'),
  ('ar','default','anerkennung.step.2.desc','شهادات أصلية، ترجمات معتمدة إلى الألمانية، السيرة الذاتية، إثبات الهوية.'),
  ('ar','default','anerkennung.step.3.title','تقديم طلب الاعتراف'),
  ('ar','default','anerkennung.step.3.desc','تقديم الطلب لدى الجهة المختصة (IHK FOSA / غرفة الحرف / السلطة الإقليمية).'),
  ('ar','default','anerkennung.step.4.title','انتظار تقييم التكافؤ'),
  ('ar','default','anerkennung.step.4.desc','تقيّم الجهة المختصة التكافؤ خلال حوالي 3 أشهر.'),
  ('ar','default','anerkennung.step.5.title','إجراء تعويضي (إذا لزم الأمر)'),
  ('ar','default','anerkennung.step.5.desc','دورة تأهيلية، اختبار معرفة أو اختبار كفاءة في حال وجود فروق جوهرية.'),
  ('ar','default','anerkennung.step.6.title','استلام قرار الاعتراف'),
  ('ar','default','anerkennung.step.6.desc','تكافؤ كامل، جزئي أو مرفوض — مع التعليل.');

-- ============================================================
-- Visa universal requirements (all visa types)
-- ============================================================
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','visa.req.passport.title','Gültiger Reisepass'),
  ('de','default','visa.req.passport.desc','Noch mindestens 6 Monate gültig nach geplanter Einreise.'),
  ('de','default','visa.req.photos.title','Biometrische Passfotos'),
  ('de','default','visa.req.photos.desc','Zwei aktuelle biometrische Passfotos (35 × 45 mm).'),
  ('de','default','visa.req.videx.title','Visumsantrag (Videx)'),
  ('de','default','visa.req.videx.desc','Ausgefüllter Antrag über videx.diplo.de.'),
  ('de','default','visa.req.insurance.title','Krankenversicherung'),
  ('de','default','visa.req.insurance.desc','Auslandskrankenversicherung oder spätere deutsche KV.'),

  ('fr','default','visa.req.passport.title','Passeport valide'),
  ('fr','default','visa.req.passport.desc','Valable au moins 6 mois après l''entrée prévue.'),
  ('fr','default','visa.req.photos.title','Photos biométriques'),
  ('fr','default','visa.req.photos.desc','Deux photos biométriques récentes (35 × 45 mm).'),
  ('fr','default','visa.req.videx.title','Formulaire Videx'),
  ('fr','default','visa.req.videx.desc','Formulaire rempli sur videx.diplo.de.'),
  ('fr','default','visa.req.insurance.title','Assurance maladie'),
  ('fr','default','visa.req.insurance.desc','Assurance voyage ou futur contrat allemand.'),

  ('ar','default','visa.req.passport.title','جواز سفر ساري المفعول'),
  ('ar','default','visa.req.passport.desc','صالح لمدة 6 أشهر على الأقل بعد الدخول المخطط.'),
  ('ar','default','visa.req.photos.title','صور بيومترية'),
  ('ar','default','visa.req.photos.desc','صورتان حديثتان (35 × 45 مم).'),
  ('ar','default','visa.req.videx.title','طلب التأشيرة (Videx)'),
  ('ar','default','visa.req.videx.desc','استمارة مكتملة عبر videx.diplo.de.'),
  ('ar','default','visa.req.insurance.title','تأمين صحي'),
  ('ar','default','visa.req.insurance.desc','تأمين سفر أو تأمين ألماني لاحقاً.');

-- Visa type specific requirements
INSERT INTO messages (locale, namespace, key, value) VALUES
  -- BLUE_CARD
  ('de','default','visa.bluecard.salary.title','Arbeitsvertrag mit Mindestgehalt'),
  ('de','default','visa.bluecard.salary.desc','Brutto ≥ 45.300 € (2024), bei Engpassberufen 41.041 €.'),
  ('de','default','visa.bluecard.degree.title','Anerkannter Hochschulabschluss'),
  ('de','default','visa.bluecard.degree.desc','Über anabin als H+ bewertet oder deutsche Anerkennung.'),
  ('de','default','visa.bluecard.ba.title','Zustimmung Bundesagentur für Arbeit'),
  ('de','default','visa.bluecard.ba.desc','Bei einigen Berufen erforderlich.'),

  ('fr','default','visa.bluecard.salary.title','Contrat de travail avec salaire minimum'),
  ('fr','default','visa.bluecard.salary.desc','Brut ≥ 45 300 € (2024), métiers en tension 41 041 €.'),
  ('fr','default','visa.bluecard.degree.title','Diplôme universitaire reconnu'),
  ('fr','default','visa.bluecard.degree.desc','Évalué H+ sur anabin ou reconnu en Allemagne.'),
  ('fr','default','visa.bluecard.ba.title','Accord Agence fédérale pour l''emploi'),
  ('fr','default','visa.bluecard.ba.desc','Requis pour certains métiers.'),

  ('ar','default','visa.bluecard.salary.title','عقد عمل بحد أدنى من الأجر'),
  ('ar','default','visa.bluecard.salary.desc','الراتب الإجمالي ≥ 45.300 € (2024)، في مهن النقص 41.041 €.'),
  ('ar','default','visa.bluecard.degree.title','شهادة جامعية معترف بها'),
  ('ar','default','visa.bluecard.degree.desc','مقيمة H+ في anabin أو معترف بها في ألمانيا.'),
  ('ar','default','visa.bluecard.ba.title','موافقة وكالة التوظيف الفيدرالية'),
  ('ar','default','visa.bluecard.ba.desc','مطلوبة لبعض المهن.'),

  -- STUDY
  ('de','default','visa.study.admission.title','Zulassung der Hochschule'),
  ('de','default','visa.study.admission.desc','Uni-Assist bzw. direkte Zulassung.'),
  ('de','default','visa.study.blocked.title','Sperrkonto 11.904 €'),
  ('de','default','visa.study.blocked.desc','Finanzierungsnachweis für ein Jahr.'),
  ('de','default','visa.study.language.title','Sprachnachweis'),
  ('de','default','visa.study.language.desc','TestDaF/DSH oder IELTS je nach Studiengang.'),

  ('fr','default','visa.study.admission.title','Admission universitaire'),
  ('fr','default','visa.study.admission.desc','Uni-Assist ou admission directe.'),
  ('fr','default','visa.study.blocked.title','Compte bloqué 11 904 €'),
  ('fr','default','visa.study.blocked.desc','Preuve de financement pour un an.'),
  ('fr','default','visa.study.language.title','Certificat linguistique'),
  ('fr','default','visa.study.language.desc','TestDaF/DSH ou IELTS selon le cursus.'),

  ('ar','default','visa.study.admission.title','قبول جامعي'),
  ('ar','default','visa.study.admission.desc','Uni-Assist أو قبول مباشر.'),
  ('ar','default','visa.study.blocked.title','حساب مجمد 11.904 €'),
  ('ar','default','visa.study.blocked.desc','إثبات التمويل لمدة سنة.'),
  ('ar','default','visa.study.language.title','شهادة لغة'),
  ('ar','default','visa.study.language.desc','TestDaF/DSH أو IELTS حسب التخصص.'),

  -- VOCATIONAL_TRAINING
  ('de','default','visa.voc.contract.title','Ausbildungsvertrag'),
  ('de','default','visa.voc.contract.desc','Schul-/Betriebsvertrag mit einer deutschen Einrichtung.'),
  ('de','default','visa.voc.german.title','Deutschkenntnisse B1'),
  ('de','default','visa.voc.german.desc','Nachweis über ein anerkanntes Zertifikat.'),
  ('de','default','visa.voc.financing.title','Finanzierungsnachweis'),
  ('de','default','visa.voc.financing.desc','Sperrkonto oder Verpflichtungserklärung.'),

  ('fr','default','visa.voc.contract.title','Contrat de formation'),
  ('fr','default','visa.voc.contract.desc','Contrat scolaire/professionnel avec une structure allemande.'),
  ('fr','default','visa.voc.german.title','Allemand B1'),
  ('fr','default','visa.voc.german.desc','Certificat reconnu.'),
  ('fr','default','visa.voc.financing.title','Preuve de financement'),
  ('fr','default','visa.voc.financing.desc','Compte bloqué ou déclaration de prise en charge.'),

  ('ar','default','visa.voc.contract.title','عقد تدريب'),
  ('ar','default','visa.voc.contract.desc','عقد مدرسي/مهني مع مؤسسة ألمانية.'),
  ('ar','default','visa.voc.german.title','ألمانية B1'),
  ('ar','default','visa.voc.german.desc','شهادة معتمدة.'),
  ('ar','default','visa.voc.financing.title','إثبات التمويل'),
  ('ar','default','visa.voc.financing.desc','حساب مجمد أو تعهد مالي.'),

  -- SKILLED_WORKER_VOCATIONAL
  ('de','default','visa.skilledVoc.recognition.title','Anerkennungsbescheid'),
  ('de','default','visa.skilledVoc.recognition.desc','Gleichwertigkeit der Berufsausbildung nachgewiesen.'),
  ('de','default','visa.skilledVoc.contract.title','Konkreter Arbeitsvertrag'),
  ('de','default','visa.skilledVoc.contract.desc','Arbeitsvertrag / verbindliches Jobangebot.'),
  ('de','default','visa.skilledVoc.german.title','Deutschkenntnisse'),
  ('de','default','visa.skilledVoc.german.desc','In der Regel B1, je nach Beruf ggf. B2.'),

  ('fr','default','visa.skilledVoc.recognition.title','Décision de reconnaissance'),
  ('fr','default','visa.skilledVoc.recognition.desc','Équivalence de formation prouvée.'),
  ('fr','default','visa.skilledVoc.contract.title','Contrat de travail concret'),
  ('fr','default','visa.skilledVoc.contract.desc','Contrat ou offre d''emploi ferme.'),
  ('fr','default','visa.skilledVoc.german.title','Niveau d''allemand'),
  ('fr','default','visa.skilledVoc.german.desc','En général B1, parfois B2 selon le métier.'),

  ('ar','default','visa.skilledVoc.recognition.title','قرار الاعتراف'),
  ('ar','default','visa.skilledVoc.recognition.desc','إثبات تكافؤ التدريب المهني.'),
  ('ar','default','visa.skilledVoc.contract.title','عقد عمل محدد'),
  ('ar','default','visa.skilledVoc.contract.desc','عقد أو عرض عمل نهائي.'),
  ('ar','default','visa.skilledVoc.german.title','مستوى الألمانية'),
  ('ar','default','visa.skilledVoc.german.desc','عادة B1، أحياناً B2 حسب المهنة.'),

  -- SKILLED_WORKER_ACADEMIC
  ('de','default','visa.skilledAcad.degree.title','Anerkannter Hochschulabschluss'),
  ('de','default','visa.skilledAcad.degree.desc','Über anabin H+ geprüft.'),
  ('de','default','visa.skilledAcad.contract.title','Passender Arbeitsvertrag'),
  ('de','default','visa.skilledAcad.contract.desc','Stelle passend zur akademischen Ausbildung.'),

  ('fr','default','visa.skilledAcad.degree.title','Diplôme universitaire reconnu'),
  ('fr','default','visa.skilledAcad.degree.desc','Vérifié H+ via anabin.'),
  ('fr','default','visa.skilledAcad.contract.title','Contrat de travail adapté'),
  ('fr','default','visa.skilledAcad.contract.desc','Poste en adéquation avec la formation universitaire.'),

  ('ar','default','visa.skilledAcad.degree.title','شهادة جامعية معترف بها'),
  ('ar','default','visa.skilledAcad.degree.desc','مفحوصة H+ عبر anabin.'),
  ('ar','default','visa.skilledAcad.contract.title','عقد عمل مناسب'),
  ('ar','default','visa.skilledAcad.contract.desc','منصب مناسب للتأهيل الأكاديمي.'),

  -- JOB_SEEKER
  ('de','default','visa.jobseeker.degree.title','Hochschulabschluss (H+ anabin)'),
  ('de','default','visa.jobseeker.degree.desc','Oder gleichwertige berufliche Qualifikation.'),
  ('de','default','visa.jobseeker.financing.title','Finanzierung für ≥ 6 Monate'),
  ('de','default','visa.jobseeker.financing.desc','Sperrkonto oder vergleichbarer Nachweis.'),

  ('fr','default','visa.jobseeker.degree.title','Diplôme universitaire (H+ anabin)'),
  ('fr','default','visa.jobseeker.degree.desc','Ou qualification professionnelle équivalente.'),
  ('fr','default','visa.jobseeker.financing.title','Financement pour ≥ 6 mois'),
  ('fr','default','visa.jobseeker.financing.desc','Compte bloqué ou preuve équivalente.'),

  ('ar','default','visa.jobseeker.degree.title','شهادة جامعية (H+ anabin)'),
  ('ar','default','visa.jobseeker.degree.desc','أو مؤهل مهني معادل.'),
  ('ar','default','visa.jobseeker.financing.title','تمويل ≥ 6 أشهر'),
  ('ar','default','visa.jobseeker.financing.desc','حساب مجمد أو إثبات معادل.'),

  -- RECOGNITION
  ('de','default','visa.recognition.decision.title','Teilanerkennungsbescheid'),
  ('de','default','visa.recognition.decision.desc','Mit konkret benannter Ausgleichsmaßnahme.'),
  ('de','default','visa.recognition.course.title','Nachweis Anpassungslehrgang/Prüfung'),
  ('de','default','visa.recognition.course.desc','Ort & Träger der Ausgleichsmaßnahme.'),

  ('fr','default','visa.recognition.decision.title','Décision de reconnaissance partielle'),
  ('fr','default','visa.recognition.decision.desc','Avec mesure compensatoire désignée.'),
  ('fr','default','visa.recognition.course.title','Justificatif stage/examen'),
  ('fr','default','visa.recognition.course.desc','Lieu et organisme de la mesure compensatoire.'),

  ('ar','default','visa.recognition.decision.title','قرار اعتراف جزئي'),
  ('ar','default','visa.recognition.decision.desc','مع تحديد إجراء تعويضي.'),
  ('ar','default','visa.recognition.course.title','إثبات الدورة/الاختبار'),
  ('ar','default','visa.recognition.course.desc','مكان وجهة الإجراء التعويضي.'),

  -- CHANCENKARTE
  ('de','default','visa.chance.points.title','Punktesystem ≥ 6 Punkte'),
  ('de','default','visa.chance.points.desc','Deutschkenntnisse, Alter, Berufserfahrung, Deutschland-Bezug.'),
  ('de','default','visa.chance.financing.title','Finanzierungsnachweis ≥ 12 Monate'),
  ('de','default','visa.chance.financing.desc','~1027 €/Monat × 12 = 12.324 €.'),

  ('fr','default','visa.chance.points.title','Barème ≥ 6 points'),
  ('fr','default','visa.chance.points.desc','Allemand, âge, expérience, lien avec l''Allemagne.'),
  ('fr','default','visa.chance.financing.title','Financement ≥ 12 mois'),
  ('fr','default','visa.chance.financing.desc','~1027 €/mois × 12 = 12 324 €.'),

  ('ar','default','visa.chance.points.title','نظام نقاط ≥ 6 نقاط'),
  ('ar','default','visa.chance.points.desc','الألمانية، العمر، الخبرة، الارتباط بألمانيا.'),
  ('ar','default','visa.chance.financing.title','إثبات تمويل ≥ 12 شهراً'),
  ('ar','default','visa.chance.financing.desc','~1027 €/شهر × 12 = 12.324 €.');

-- ============================================================
-- Domain error keys thrown by services
-- ============================================================
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','default','error.jobs.notOwner','Du bist nicht der Besitzer dieser Stelle.'),
  ('de','default','error.jobs.salaryRange','Das Mindestgehalt darf nicht über dem Höchstgehalt liegen.'),
  ('de','default','error.applications.duplicate','Du hast dich bereits auf diese Stelle beworben.'),
  ('de','default','error.companies.slugTaken','Dieser Firmen-Slug ist bereits vergeben.'),
  ('de','default','error.companies.notOwner','Du bist nicht der Besitzer dieser Firma.'),
  ('de','default','error.documents.empty','Die hochgeladene Datei ist leer.'),
  ('de','default','error.documents.notOwner','Du bist nicht der Besitzer dieses Dokuments.'),
  ('de','default','error.documents.notCv','Das Dokument ist kein Lebenslauf.'),
  ('de','default','error.profile.notFound','Profil nicht gefunden.'),

  ('fr','default','error.jobs.notOwner','Vous n''êtes pas propriétaire de cette offre.'),
  ('fr','default','error.jobs.salaryRange','Le salaire minimum ne peut être supérieur au maximum.'),
  ('fr','default','error.applications.duplicate','Vous avez déjà postulé à cette offre.'),
  ('fr','default','error.companies.slugTaken','Ce slug d''entreprise est déjà pris.'),
  ('fr','default','error.companies.notOwner','Vous n''êtes pas propriétaire de cette entreprise.'),
  ('fr','default','error.documents.empty','Le fichier téléversé est vide.'),
  ('fr','default','error.documents.notOwner','Vous n''êtes pas propriétaire de ce document.'),
  ('fr','default','error.documents.notCv','Le document n''est pas un CV.'),
  ('fr','default','error.profile.notFound','Profil introuvable.'),

  ('ar','default','error.jobs.notOwner','أنت لست مالك هذا العرض.'),
  ('ar','default','error.jobs.salaryRange','الحد الأدنى للأجر لا يمكن أن يتجاوز الحد الأقصى.'),
  ('ar','default','error.applications.duplicate','لقد قدمت بالفعل لهذا العرض.'),
  ('ar','default','error.companies.slugTaken','هذا المعرف المختصر للشركة مستخدم بالفعل.'),
  ('ar','default','error.companies.notOwner','أنت لست مالك هذه الشركة.'),
  ('ar','default','error.documents.empty','الملف المرفوع فارغ.'),
  ('ar','default','error.documents.notOwner','أنت لست مالك هذا المستند.'),
  ('ar','default','error.documents.notCv','المستند ليس سيرة ذاتية.'),
  ('ar','default','error.profile.notFound','الملف الشخصي غير موجود.');

-- ============================================================
-- Email template bodies used by notification-service
-- ============================================================
INSERT INTO messages (locale, namespace, key, value) VALUES
  ('de','email','newMatches.body','Hallo, wir haben {0} neue passende Stellen für dich gefunden. Jetzt ansehen: {1}'),
  ('fr','email','newMatches.body','Bonjour, nous avons trouvé {0} nouvelles offres pour vous. Voir : {1}'),
  ('ar','email','newMatches.body','مرحباً، وجدنا {0} عروض جديدة تناسبك. اطلع: {1}');
