package com.example.ui.localization

import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.AppLanguage
import com.example.data.model.OrderStatus
import com.example.data.model.RequestType

object AppStrings {

    fun getLayoutDirection(language: AppLanguage): LayoutDirection {
        return if (language == AppLanguage.DARIJA) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "سخرة شفشاون"
        AppLanguage.FRENCH -> "Chaouen Express"
    }

    fun appSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "توصيل فوري بالمدينة الزرقاء"
        AppLanguage.FRENCH -> "Livraison rapide dans la Perle Bleue"
    }

    fun roleCustomer(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "الزبون"
        AppLanguage.FRENCH -> "Client"
    }

    fun roleCourier(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "الليفروغ"
        AppLanguage.FRENCH -> "Livreur"
    }

    fun roleAdmin(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "الإدارة"
        AppLanguage.FRENCH -> "Admin"
    }

    fun requestTypeTitle(type: RequestType, lang: AppLanguage): String = when (type) {
        RequestType.BUY_FOR_ME -> when (lang) {
            AppLanguage.DARIJA -> "شري ليا وخلس (Achetez pour moi)"
            AppLanguage.FRENCH -> "Achetez pour moi (Le livreur avance les frais)"
        }
        RequestType.PICKUP_ONLY -> when (lang) {
            AppLanguage.DARIJA -> "موصي عليها غير جيبها (Récupérer colis)"
            AppLanguage.FRENCH -> "Commande prête (Récupération et livraison)"
        }
    }

    fun requestTypeSubtitle(type: RequestType, lang: AppLanguage): String = when (type) {
        RequestType.BUY_FOR_ME -> when (lang) {
            AppLanguage.DARIJA -> "الليفروغ يخلص من عندو وترد ليه كاش عند الباب"
            AppLanguage.FRENCH -> "Le livreur achète au magasin et vous réglez en espèces à l'arrivée"
        }
        RequestType.PICKUP_ONLY -> when (lang) {
            AppLanguage.DARIJA -> "طلبية مخلصة أو واجدة، الليفروغ غير يجيبها ليك"
            AppLanguage.FRENCH -> "La commande est déjà réglée ou prête, le livreur la récupère"
        }
    }

    fun statusLabel(status: OrderStatus, lang: AppLanguage): String = when (status) {
        OrderStatus.REQUESTED -> when (lang) {
            AppLanguage.DARIJA -> "في انتظار موافقة الليفروغ"
            AppLanguage.FRENCH -> "En attente du livreur"
        }
        OrderStatus.ACCEPTED -> when (lang) {
            AppLanguage.DARIJA -> "قبل الليفروغ الطلب"
            AppLanguage.FRENCH -> "Commande acceptée"
        }
        OrderStatus.AT_SHOP -> when (lang) {
            AppLanguage.DARIJA -> "وصل الليفروغ للمحل"
            AppLanguage.FRENCH -> "Livreur au magasin"
        }
        OrderStatus.PURCHASED -> when (lang) {
            AppLanguage.DARIJA -> "تم الشراء / الاستلام بنجاح"
            AppLanguage.FRENCH -> "Articles récupérés"
        }
        OrderStatus.ON_THE_WAY -> when (lang) {
            AppLanguage.DARIJA -> "في الطريق إليك"
            AppLanguage.FRENCH -> "En route vers votre adresse"
        }
        OrderStatus.DELIVERED -> when (lang) {
            AppLanguage.DARIJA -> "تم التسليم بنجاح"
            AppLanguage.FRENCH -> "Livraison terminée"
        }
        OrderStatus.CANCELLED -> when (lang) {
            AppLanguage.DARIJA -> "الطلب ملغى / اعتذر الليفروغ"
            AppLanguage.FRENCH -> "Commande annulée"
        }
    }

    fun currencyMad(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "درهم"
        AppLanguage.FRENCH -> "MAD"
    }

    fun deliveryLocationTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "1. حدد موقع التوصيل بشفشاون"
        AppLanguage.FRENCH -> "1. Lieu de livraison à Chefchaouen"
    }

    fun insideServiceArea(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "داخل حيز خدمة شفشاون المعتمد"
        AppLanguage.FRENCH -> "Dans la zone de service Chefchaouen"
    }

    fun outsideServiceArea(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "خارج حيز خدمة شفشاون المعتمد"
        AppLanguage.FRENCH -> "Hors de la zone couverte"
    }

    fun selectCourierTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "2. اختر الليفروغ القريب منك"
        AppLanguage.FRENCH -> "2. Choisissez votre livreur"
    }

    fun orderDetailsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "3. تفاصيل السخرة والمحل"
        AppLanguage.FRENCH -> "3. Détails de la course"
    }

    fun shopPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "اسم المحل، السناگ أو المطعم (مثال: سناك الزهرة، صيدلية وطاء الحمام)"
        AppLanguage.FRENCH -> "Nom du commerce ou restaurant (ex: Snack Kasbah, Pharmacie Outa)"
    }

    fun itemsPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "شنو باغي تجيب؟ اكتب بالتفصيل (طاجين، خبز الدار، دواء، حلويات...)"
        AppLanguage.FRENCH -> "Décrivez vos articles (repas, pain traditionnel, médicaments...)"
    }

    fun estimatedCostLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "المبلغ التقريبي للسلعة (درهم)"
        AppLanguage.FRENCH -> "Coût estimé des achats (MAD)"
    }

    fun deliveryFeeLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "واجب التوصيل"
        AppLanguage.FRENCH -> "Frais de livraison"
    }

    fun cashOnlyNote(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "الأداء كاش عند الاستلام فقط (لا توجد بطاقة بنكية)"
        AppLanguage.FRENCH -> "Paiement en espèces à la livraison uniquement"
    }

    fun confirmOrderBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "تأكيد وطلب السخرة الآن"
        AppLanguage.FRENCH -> "Confirmer la commande"
    }

    fun callBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "اتصال هاتفي عادي"
        AppLanguage.FRENCH -> "Appel téléphonique"
    }

    fun whatsappBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "مراسلة واتساب"
        AppLanguage.FRENCH -> "Message WhatsApp"
    }

    fun whatsappCallDisclaimer(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "ملاحظة: لإجراء مكالمة صوتية في واتساب، اضغط على الزر وافتح المحادثة ثم ابدأ الاتصال الصوتي من داخل التطبيق."
        AppLanguage.FRENCH -> "Note : Pour un appel vocal WhatsApp, ouvrez la discussion puis lancez l'appel vocal depuis WhatsApp."
    }

    fun chooseAnotherCourier(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "اختيار ليفروغ آخر"
        AppLanguage.FRENCH -> "Choisir un autre livreur"
    }

    fun courierPurchaseLimitLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "سقف الشراء النقدي"
        AppLanguage.FRENCH -> "Plafond d'achat"
    }

    fun courierPendingApproval(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "الحساب قيد المراجعة والموافقة من طرف الإدارة"
        AppLanguage.FRENCH -> "Compte en attente d'approbation par l'administrateur"
    }

    fun courierApprovedStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "حساب معتمد ومفعل"
        AppLanguage.FRENCH -> "Compte vérifié et approuvé"
    }

    fun availableStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "متاح للطلبات الآن"
        AppLanguage.FRENCH -> "Disponible pour les courses"
    }

    fun offlineStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "غير متاح (متوقف عن العمل)"
        AppLanguage.FRENCH -> "Indisponible"
    }

    // --- Client & Courier Registration Strings ---
    fun registerNewClientTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "تسجيل زبون جديد"
        AppLanguage.FRENCH -> "Inscrire un nouveau client"
    }

    fun registerNewCourierTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "تسجيل ليفروغ جديد"
        AppLanguage.FRENCH -> "Devenir livreur à Chefchaouen"
    }

    fun switchAccountTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "تبديل الحساب"
        AppLanguage.FRENCH -> "Changer de profil"
    }

    fun fullNameLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "الاسم الكامل (Nom complet)"
        AppLanguage.FRENCH -> "Nom complet"
    }

    fun phoneLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "رقم الهاتف المغربي (+212 6... / +212 7...)"
        AppLanguage.FRENCH -> "Numéro de téléphone (+212...)"
    }

    fun addressLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "الحي أو العنوان بشفشاون (مثال: حي الأندلس، درب الصور)"
        AppLanguage.FRENCH -> "Quartier ou adresse à Chefchaouen"
    }

    fun vehicleTypeLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "وسيلة التنقل"
        AppLanguage.FRENCH -> "Moyen de transport"
    }

    fun purchaseLimitInputLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "سقف الشراء النقدي الأولي (درهم)"
        AppLanguage.FRENCH -> "Plafond d'achat initial (MAD)"
    }

    fun confirmRegisterBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "تأكيد وإنشاء الحساب"
        AppLanguage.FRENCH -> "Créer mon compte"
    }

    fun cancelBtn(lang: AppLanguage): String = when (lang) {
        AppLanguage.DARIJA -> "إلغاء"
        AppLanguage.FRENCH -> "Annuler"
    }
}
