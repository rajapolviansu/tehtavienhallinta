package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.datepicker.DatePicker;

import java.util.List;
import java.util.Locale;

/**
 * Päivämääräkenttä, jonka kalenteri on suomeksi (kuukaudet, viikonpäivät, viikon alku maanantaina).
 */
class FinnishDatePicker extends DatePicker {

    FinnishDatePicker(String label) {
        super(label);
        setLocale(Locale.of("fi", "FI"));

        DatePickerI18n i18n = new DatePickerI18n();
        i18n.setMonthNames(List.of("tammikuu", "helmikuu", "maaliskuu", "huhtikuu", "toukokuu",
                "kesäkuu", "heinäkuu", "elokuu", "syyskuu", "lokakuu", "marraskuu", "joulukuu"));
        i18n.setWeekdays(List.of("sunnuntai", "maanantai", "tiistai", "keskiviikko", "torstai",
                "perjantai", "lauantai"));
        i18n.setWeekdaysShort(List.of("su", "ma", "ti", "ke", "to", "pe", "la"));
        i18n.setFirstDayOfWeek(1);
        i18n.setToday("Tänään");
        i18n.setCancel("Peruuta");
        setI18n(i18n);
    }
}
