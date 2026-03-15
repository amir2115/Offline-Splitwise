package com.encer.offlinesplitwise.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.encer.offlinesplitwise.data.preferences.AppLanguage
import com.encer.offlinesplitwise.domain.MessageKey

data class AppStrings(
    val appTitle: String,
    val homeTab: String,
    val settingsTab: String,
    val addGroup: String,
    val newGroupTitle: String,
    val editGroupTitle: String,
    val groupPlaceholder: String,
    val createGroup: String,
    val save: String,
    val cancel: String,
    val noGroupsTitle: String,
    val noGroupsSubtitle: String,
    val homeHeroTitle: String,
    val homeHeroSubtitle: String,
    val groupFallbackTitle: String,
    val groupOverviewTitle: String,
    val groupOverviewSubtitle: String,
    val totalExpenseLabel: String,
    val membersLabel: String,
    val settlementsLabel: String,
    val openBalancesLabel: String,
    val membersAction: String,
    val newExpenseAction: String,
    val addSettlementAction: String,
    val balancesAction: String,
    val recentExpensesTitle: String,
    val recentSettlementsTitle: String,
    val noExpensesTitle: String,
    val noExpensesSubtitle: String,
    val noSettlementsTitle: String,
    val noSettlementsSubtitle: String,
    val membersOfGroupPrefix: String,
    val addMember: String,
    val editMember: String,
    val memberPlaceholder: String,
    val saveMember: String,
    val noMembersTitle: String,
    val noMembersSubtitle: String,
    val addExpenseTitle: String,
    val editExpenseTitle: String,
    val expenseHeroTitle: String,
    val expenseHeroSubtitle: String,
    val expenseTitleLabel: String,
    val expenseNoteLabel: String,
    val totalAmountLabel: String,
    val equalSplitLabel: String,
    val exactSplitLabel: String,
    val membersAndPayersTitle: String,
    val saveExpense: String,
    val saveExpenseChanges: String,
    val addSettlementTitle: String,
    val editSettlementTitle: String,
    val settlementHeroTitle: String,
    val settlementHeroSubtitle: String,
    val payerLabel: String,
    val receiverLabel: String,
    val settlementAmountLabel: String,
    val noteLabel: String,
    val saveSettlement: String,
    val saveSettlementChanges: String,
    val balancesPrefix: String,
    val optimizePaymentsTitle: String,
    val optimizePaymentsSubtitle: String,
    val memberBalanceTitle: String,
    val suggestedPaymentsTitle: String,
    val allSettledTitle: String,
    val allSettledSubtitle: String,
    val expenseDetailsFallback: String,
    val expenseNotFound: String,
    val noDescription: String,
    val totalAmountStat: String,
    val splitMethodStat: String,
    val dateStat: String,
    val peopleCountStat: String,
    val payersTitle: String,
    val sharesTitle: String,
    val paidHowMuchLabel: String,
    val shareAmountLabel: String,
    val equalShareLabel: String,
    val creditorLabel: String,
    val debtorLabel: String,
    val settledLabel: String,
    val paidStat: String,
    val owedStat: String,
    val netStat: String,
    val edit: String,
    val delete: String,
    val back: String,
    val settingsHeroTitle: String,
    val settingsHeroSubtitle: String,
    val languageTitle: String,
    val themeTitle: String,
    val persianLabel: String,
    val englishLabel: String,
    val lightLabel: String,
    val darkLabel: String,
) {
    fun memberSince(date: String): String =
        if (appTitle == "Offline Splitwise") "Member since $date" else "عضو از $date"

    fun paymentSuggestion(from: String, to: String): String =
        if (appTitle == "Offline Splitwise") "$from should pay $to" else "$from باید به $to پرداخت کند"

    fun payersSummary(names: String): String =
        if (appTitle == "Offline Splitwise") "Payers: $names" else "پرداخت‌کننده‌ها: $names"

    fun settlementSummary(from: String, to: String): String =
        if (appTitle == "Offline Splitwise") "$from paid $to" else "$from به $to"

    fun membersOfGroup(groupName: String): String =
        if (appTitle == "Offline Splitwise") "$membersOfGroupPrefix $groupName" else "$membersOfGroupPrefix $groupName"

    fun balancesOfGroup(groupName: String): String =
        if (appTitle == "Offline Splitwise") "$balancesPrefix $groupName" else "$balancesPrefix $groupName"

    fun memberFallback(memberId: Long): String =
        if (appTitle == "Offline Splitwise") "Member $memberId" else "کاربر $memberId"

    fun expenseFormTitle(isEdit: Boolean): String = if (isEdit) editExpenseTitle else addExpenseTitle
    fun expenseFormAction(isEdit: Boolean): String = if (isEdit) saveExpenseChanges else saveExpense
    fun settlementFormTitle(isEdit: Boolean): String = if (isEdit) editSettlementTitle else addSettlementTitle
    fun settlementFormAction(isEdit: Boolean): String = if (isEdit) saveSettlementChanges else saveSettlement
    fun splitTypeLabel(isEqual: Boolean): String = if (isEqual) equalSplitLabel else exactSplitLabel
    fun balanceState(net: Int): String = when {
        net > 0 -> creditorLabel
        net < 0 -> debtorLabel
        else -> settledLabel
    }

    fun message(key: MessageKey?): String? = when (key) {
        null -> null
        MessageKey.EXPENSE_TOTAL_POSITIVE -> if (appTitle == "Offline Splitwise") "Total amount must be greater than zero." else "مبلغ کل باید بیشتر از صفر باشد."
        MessageKey.EXPENSE_AT_LEAST_ONE_PAYER -> if (appTitle == "Offline Splitwise") "At least one payer is required." else "حداقل یک پرداخت‌کننده لازم است."
        MessageKey.EXPENSE_AT_LEAST_ONE_SHARE -> if (appTitle == "Offline Splitwise") "Select at least one participant." else "حداقل یک نفر باید در تقسیم حضور داشته باشد."
        MessageKey.EXPENSE_NEGATIVE_VALUES -> if (appTitle == "Offline Splitwise") "Negative amounts are not allowed." else "مقادیر منفی مجاز نیستند."
        MessageKey.EXPENSE_PAYER_TOTAL_MISMATCH -> if (appTitle == "Offline Splitwise") "Payer totals must match the full amount." else "جمع پرداخت‌کننده‌ها باید با مبلغ کل برابر باشد."
        MessageKey.EXPENSE_SHARE_TOTAL_MISMATCH -> if (appTitle == "Offline Splitwise") "Shares must match the full amount." else "جمع سهم‌ها باید با مبلغ کل برابر باشد."
        MessageKey.EXPENSE_TITLE_REQUIRED -> if (appTitle == "Offline Splitwise") "Enter an expense title." else "عنوان خرج را وارد کنید."
        MessageKey.EXPENSE_SAVED -> if (appTitle == "Offline Splitwise") "Expense saved." else "خرج ذخیره شد."
        MessageKey.SETTLEMENT_SELECT_TWO_MEMBERS -> if (appTitle == "Offline Splitwise") "Select two members." else "دو نفر را انتخاب کنید."
        MessageKey.SETTLEMENT_MEMBERS_MUST_DIFFER -> if (appTitle == "Offline Splitwise") "Payer and receiver must be different." else "پرداخت‌کننده و دریافت‌کننده باید متفاوت باشند."
        MessageKey.SETTLEMENT_AMOUNT_POSITIVE -> if (appTitle == "Offline Splitwise") "Settlement amount must be greater than zero." else "مبلغ تسویه باید بیشتر از صفر باشد."
        MessageKey.SETTLEMENT_SAVED -> if (appTitle == "Offline Splitwise") "Settlement saved." else "تسویه ذخیره شد."
    }
}

private val faStrings = AppStrings(
    appTitle = "خرج‌یار آفلاین",
    homeTab = "خانه",
    settingsTab = "تنظیمات",
    addGroup = "افزودن گروه",
    newGroupTitle = "گروه جدید",
    editGroupTitle = "ویرایش گروه",
    groupPlaceholder = "مثلا سفر شمال",
    createGroup = "ساخت گروه",
    save = "ذخیره",
    cancel = "انصراف",
    noGroupsTitle = "هنوز گروهی نداری",
    noGroupsSubtitle = "از دکمه بالا یک گروه جدید بساز.",
    homeHeroTitle = "خرج‌ها را آفلاین جمع کن",
    homeHeroSubtitle = "برای هر سفر یا جمع، گروه بساز، اعضا را اضافه کن و بدهی‌ها را با حالت simplify ببین.",
    groupFallbackTitle = "گروه",
    groupOverviewTitle = "وضعیت کلی گروه",
    groupOverviewSubtitle = "خرج‌ها، اعضا، تسویه‌ها و مانده‌های باز را از همین‌جا کنترل کن.",
    totalExpenseLabel = "کل خرج",
    membersLabel = "اعضا",
    settlementsLabel = "تسویه‌ها",
    openBalancesLabel = "مانده باز",
    membersAction = "اعضا",
    newExpenseAction = "خرج جدید",
    addSettlementAction = "ثبت تسویه",
    balancesAction = "مانده‌ها",
    recentExpensesTitle = "آخرین خرج‌ها",
    recentSettlementsTitle = "تسویه‌های اخیر",
    noExpensesTitle = "خرجی ثبت نشده",
    noExpensesSubtitle = "اولین خرج گروه را ثبت کن تا مانده‌ها محاسبه شوند.",
    noSettlementsTitle = "تسویه‌ای ثبت نشده",
    noSettlementsSubtitle = "وقتی کسی بدهی‌اش را داد، از اینجا ثبتش کن.",
    membersOfGroupPrefix = "اعضای",
    addMember = "افزودن عضو",
    editMember = "ویرایش عضو",
    memberPlaceholder = "نام عضو",
    saveMember = "ثبت عضو",
    noMembersTitle = "هیچ عضوی ثبت نشده",
    noMembersSubtitle = "برای ثبت خرج حداقل یک عضو نیاز داری.",
    addExpenseTitle = "خرج جدید",
    editExpenseTitle = "ویرایش خرج",
    expenseHeroTitle = "خرج را دقیق ثبت کن",
    expenseHeroSubtitle = "می‌توانی مشخص کنی چه کسی پرداخت کرده و سهم هر نفر چقدر بوده.",
    expenseTitleLabel = "عنوان خرج",
    expenseNoteLabel = "توضیح",
    totalAmountLabel = "مبلغ کل (تومان)",
    equalSplitLabel = "تقسیم مساوی",
    exactSplitLabel = "مبلغ دقیق",
    membersAndPayersTitle = "اعضا و پرداخت‌کننده‌ها",
    saveExpense = "ثبت خرج",
    saveExpenseChanges = "ذخیره تغییرات",
    addSettlementTitle = "ثبت تسویه",
    editSettlementTitle = "ویرایش تسویه",
    settlementHeroTitle = "تسویه واقعی را ثبت کن",
    settlementHeroSubtitle = "وقتی یکی بدهی‌اش را به دیگری پرداخت کرد، اینجا ثبتش کن تا مانده‌ها اصلاح شوند.",
    payerLabel = "پرداخت‌کننده",
    receiverLabel = "دریافت‌کننده",
    settlementAmountLabel = "مبلغ تسویه",
    noteLabel = "یادداشت",
    saveSettlement = "ثبت تسویه",
    saveSettlementChanges = "ذخیره تغییرات",
    balancesPrefix = "مانده‌های",
    optimizePaymentsTitle = "حالت پرداخت بهینه",
    optimizePaymentsSubtitle = "کمترین تعداد پرداخت پیشنهادی را نشان می‌دهد.",
    memberBalanceTitle = "مانده هر نفر",
    suggestedPaymentsTitle = "پرداخت‌های پیشنهادی",
    allSettledTitle = "همه‌چیز تسویه است",
    allSettledSubtitle = "در این گروه پرداخت باز باقی نمانده.",
    expenseDetailsFallback = "جزئیات خرج",
    expenseNotFound = "خرج پیدا نشد.",
    noDescription = "بدون توضیح",
    totalAmountStat = "مبلغ کل",
    splitMethodStat = "روش تقسیم",
    dateStat = "تاریخ",
    peopleCountStat = "تعداد افراد",
    payersTitle = "پرداخت‌کننده‌ها",
    sharesTitle = "سهم افراد",
    paidHowMuchLabel = "چقدر پرداخت کرده؟",
    shareAmountLabel = "سهم این نفر",
    equalShareLabel = "سهم مساوی",
    creditorLabel = "طلبکار",
    debtorLabel = "بدهکار",
    settledLabel = "تسویه",
    paidStat = "پرداخت",
    owedStat = "سهم",
    netStat = "خالص",
    edit = "ویرایش",
    delete = "حذف",
    back = "بازگشت",
    settingsHeroTitle = "ظاهر و زبان اپ را تنظیم کن",
    settingsHeroSubtitle = "از اینجا بین فارسی و انگلیسی و بین تم روشن و تیره جابه‌جا شو.",
    languageTitle = "زبان",
    themeTitle = "تم",
    persianLabel = "فارسی",
    englishLabel = "English",
    lightLabel = "روشن",
    darkLabel = "تیره",
)

private val enStrings = AppStrings(
    appTitle = "Offline Splitwise",
    homeTab = "Home",
    settingsTab = "Settings",
    addGroup = "Add group",
    newGroupTitle = "New group",
    editGroupTitle = "Edit group",
    groupPlaceholder = "For example: North trip",
    createGroup = "Create group",
    save = "Save",
    cancel = "Cancel",
    noGroupsTitle = "No groups yet",
    noGroupsSubtitle = "Create your first group from the button above.",
    homeHeroTitle = "Track shared costs offline",
    homeHeroSubtitle = "Create a group for each trip or circle, add members, and keep debts simplified.",
    groupFallbackTitle = "Group",
    groupOverviewTitle = "Group overview",
    groupOverviewSubtitle = "Control expenses, members, settlements, and open balances from here.",
    totalExpenseLabel = "Total spent",
    membersLabel = "Members",
    settlementsLabel = "Settlements",
    openBalancesLabel = "Open balances",
    membersAction = "Members",
    newExpenseAction = "New expense",
    addSettlementAction = "Settle up",
    balancesAction = "Balances",
    recentExpensesTitle = "Recent expenses",
    recentSettlementsTitle = "Recent settlements",
    noExpensesTitle = "No expenses yet",
    noExpensesSubtitle = "Add the first expense to start calculating balances.",
    noSettlementsTitle = "No settlements yet",
    noSettlementsSubtitle = "Record a payment here whenever someone settles up.",
    membersOfGroupPrefix = "Members of",
    addMember = "Add member",
    editMember = "Edit member",
    memberPlaceholder = "Member name",
    saveMember = "Save member",
    noMembersTitle = "No members yet",
    noMembersSubtitle = "You need at least one member before adding expenses.",
    addExpenseTitle = "New expense",
    editExpenseTitle = "Edit expense",
    expenseHeroTitle = "Capture the expense clearly",
    expenseHeroSubtitle = "Specify who paid and how much each member actually owes.",
    expenseTitleLabel = "Expense title",
    expenseNoteLabel = "Note",
    totalAmountLabel = "Total amount (Toman)",
    equalSplitLabel = "Equal split",
    exactSplitLabel = "Exact amounts",
    membersAndPayersTitle = "Members and payers",
    saveExpense = "Save expense",
    saveExpenseChanges = "Save changes",
    addSettlementTitle = "New settlement",
    editSettlementTitle = "Edit settlement",
    settlementHeroTitle = "Record a real settlement",
    settlementHeroSubtitle = "When someone pays another member back, store it here so balances update.",
    payerLabel = "Payer",
    receiverLabel = "Receiver",
    settlementAmountLabel = "Settlement amount",
    noteLabel = "Note",
    saveSettlement = "Save settlement",
    saveSettlementChanges = "Save changes",
    balancesPrefix = "Balances for",
    optimizePaymentsTitle = "Simplified payments",
    optimizePaymentsSubtitle = "Shows the minimum suggested number of payments.",
    memberBalanceTitle = "Each member balance",
    suggestedPaymentsTitle = "Suggested payments",
    allSettledTitle = "Everything is settled",
    allSettledSubtitle = "There are no outstanding payments in this group.",
    expenseDetailsFallback = "Expense details",
    expenseNotFound = "Expense not found.",
    noDescription = "No description",
    totalAmountStat = "Total amount",
    splitMethodStat = "Split method",
    dateStat = "Date",
    peopleCountStat = "People",
    payersTitle = "Payers",
    sharesTitle = "Shares",
    paidHowMuchLabel = "How much did they pay?",
    shareAmountLabel = "This member's share",
    equalShareLabel = "Equal share",
    creditorLabel = "Creditor",
    debtorLabel = "Debtor",
    settledLabel = "Settled",
    paidStat = "Paid",
    owedStat = "Owed",
    netStat = "Net",
    edit = "Edit",
    delete = "Delete",
    back = "Back",
    settingsHeroTitle = "Tune the app language and look",
    settingsHeroSubtitle = "Switch between Persian and English, and between light and dark themes.",
    languageTitle = "Language",
    themeTitle = "Theme",
    persianLabel = "فارسی",
    englishLabel = "English",
    lightLabel = "Light",
    darkLabel = "Dark",
)

val LocalAppStrings = staticCompositionLocalOf { faStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.FA }

fun stringsFor(language: AppLanguage): AppStrings = if (language == AppLanguage.EN) enStrings else faStrings

@Composable
fun appStrings(): AppStrings = LocalAppStrings.current
