package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.babs_administration.domain.PersonFullName;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetuigenItemFormDtoTest {

    @Test
    void vanGetuigen_maaktJuisteAantalItems() {
        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(4, List.of());

        assertThat(items).hasSize(4);
    }

    @Test
    void vanGetuigen_setsVolgnummerCorrect() {
        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(3, List.of());

        assertThat(items.get(0).getVolgnummer()).isEqualTo(1);
        assertThat(items.get(1).getVolgnummer()).isEqualTo(2);
        assertThat(items.get(2).getVolgnummer()).isEqualTo(3);
    }

    @Test
    void vanGetuigen_laaatNaamNullAlsGeenBestaandeGetuige() {
        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(2, List.of());

        assertThat(items.get(0).getNaam()).isNull();
        assertThat(items.get(1).getNaam()).isNull();
    }

    @Test
    void vanGetuigen_vultnaamInVanBestaandeGetuige() {
        List<GetuigeDto> bestaande = List.of(new GetuigeDto(2, "Anna van Bergen"));

        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(3, bestaande);

        assertThat(items.get(0).getNaam()).isNull();
        assertThat(items.get(1).getNaam()).isEqualTo(new PersonFullName("Anna van Bergen"));
        assertThat(items.get(2).getNaam()).isNull();
    }

    @Test
    void vanGetuigen_meerdereBestaandeGetuigenWordenGemaptOpVolgnummer() {
        List<GetuigeDto> bestaande = List.of(
                new GetuigeDto(3, "Piet van Dijk"),
                new GetuigeDto(1, "Klaas Jansen")
        );

        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(3, bestaande);

        assertThat(items.get(0).getNaam()).isEqualTo(new PersonFullName("Klaas Jansen"));
        assertThat(items.get(1).getNaam()).isNull();
        assertThat(items.get(2).getNaam()).isEqualTo(new PersonFullName("Piet van Dijk"));
    }

    @Test
    void vanGetuigen_negerteTeKorteNaam() {
        List<GetuigeDto> bestaande = List.of(new GetuigeDto(1, "Jan"));

        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(1, bestaande);

        assertThat(items.get(0).getNaam()).isNull();
    }

    @Test
    void vanGetuigen_negerteBlanckeNaam() {
        List<GetuigeDto> bestaande = List.of(new GetuigeDto(1, "   "));

        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(1, bestaande);

        assertThat(items.get(0).getNaam()).isNull();
    }

    @Test
    void vanGetuigen_negerteNullNaam() {
        List<GetuigeDto> bestaande = List.of(new GetuigeDto(1, null));

        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(1, bestaande);

        assertThat(items.get(0).getNaam()).isNull();
    }
}
