package javaCore.customLinkedList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class CustomLinkedListImplTest {

    private static Stream<Arguments> checkAddition() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), "New Elem", 1),
                Arguments.of(List.of("EL_1", "EL_2"), "New Elem", 3)
        );
    }

    private static Stream<Arguments> checkGettingByIndexThrows() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), 1),
                Arguments.of(List.of("EL_1", "EL_2"), 2),
                Arguments.of(List.of("EL_1", "EL_2"), -1)
        );
    }

    private static Stream<Arguments> checkAddingByIndex() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), 0, "New_Elem", 1),
                Arguments.of(List.of("EL_1", "EL_2"), 0, "New_Elem", 3),
                Arguments.of(List.of("EL_1", "EL_2"), 2, "New_Elem", 3),
                Arguments.of(List.of("EL_1", "EL_2"), 1, "New_Elem", 3),
                Arguments.of(List.of("EL_1", "EL_2", "EL_3"), 1, "New_Elem", 4),
                Arguments.of(
                        IntStream.rangeClosed(1, 10)
                                 .mapToObj(i -> "El_" + i)
                                 .toList(), 7, "New Elem", 11)
        );
    }

    private static Stream<Arguments> checkAddingByIndexThrows() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), 1, "New_Elem"),
                Arguments.of(List.of("EL_1", "EL_2"), 3, "New_Elem"),
                Arguments.of(List.of("EL_1", "EL_2"), -1, "New_Elem")
        );
    }

    private static Stream<Arguments> checkRemoveByIndex() {
        return Stream.of(
                Arguments.of(List.of("EL_1", "EL_2"), 0, "EL_1", 1),
                Arguments.of(List.of("EL_1", "EL_2"), 1, "EL_2", 1),
                Arguments.of(List.of("EL_1", "EL_2", "EL_3"), 1, "EL_2", 2),
                Arguments.of(List.of("EL_1", "EL_2", "EL_3", "EL_4"), 2, "EL_3", 3)
        );
    }

    private static Stream<Arguments> checkRemoveByIndexThrows() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), 0),
                Arguments.of(Collections.emptyList(), 1),
                Arguments.of(List.of("EL_1", "EL_2"), 3),
                Arguments.of(List.of("EL_1", "EL_2"), -1)
        );
    }

    private CustomList<String> getPrepopulatedCustomLinkedList(List<String> initList) {
        CustomList<String> list = new CustomLinkedListImpl<>();
        initList.forEach(list::addLast);
        return list;
    }

    @ParameterizedTest
    @MethodSource("checkAddition")
    void addFirst(List<String> initList, String expectedElement, int expectedSize) {
        CustomList<String> list = new CustomLinkedListImpl<>();
        initList.forEach(list::addFirst);

        list.addFirst(expectedElement);

        assertEquals(expectedSize, list.size());
        assertEquals(expectedElement, list.get(0));
    }

    @ParameterizedTest
    @MethodSource("checkAddition")
    void addLast(List<String> initList, String expectedElement, int expectedSize) {
        CustomList<String> list = getPrepopulatedCustomLinkedList(initList);

        list.addLast(expectedElement);

        assertEquals(expectedSize, list.size());
        assertEquals(expectedElement, list.get(expectedSize - 1));
    }

    @ParameterizedTest
    @MethodSource("checkGettingByIndexThrows")
    void getByIndexShouldThrowException(List<String> initList, int index) {
        CustomList<String> list = getPrepopulatedCustomLinkedList(initList);

        assertThrowsExactly(IllegalArgumentException.class, () -> list.get(index));
    }

    @ParameterizedTest
    @MethodSource("checkAddingByIndex")
    void addByIndexPositive(List<String> initList, int index, String expectedElement, int expectedSize) {
        CustomList<String> list = getPrepopulatedCustomLinkedList(initList);

        list.add(index, expectedElement);

        assertEquals(expectedSize, list.size());
        assertEquals(expectedElement, list.get(index));
    }

    @ParameterizedTest
    @MethodSource("checkAddingByIndexThrows")
    void addByIndexShouldThrowException(List<String> initList, int index, String expectedElement) {
        CustomList<String> list = getPrepopulatedCustomLinkedList(initList);

        assertThrowsExactly(IllegalArgumentException.class, () -> list.add(index, expectedElement));

    }

    @Test
    void getFirstPositive() {
        String expectedResult = "El_1";
        CustomList<String> list = getPrepopulatedCustomLinkedList(List.of(expectedResult, "El_2"));

        String actualResult = list.getFirst();

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getFirstShouldThrow() {
        CustomList<String> list = getPrepopulatedCustomLinkedList(Collections.emptyList());

        assertThrowsExactly(NoSuchElementException.class, list::getFirst);
    }

    @Test
    void getLastPositive() {
        String expectedResult = "El_2";
        CustomList<String> list = getPrepopulatedCustomLinkedList(List.of("El_1", expectedResult));

        String actualResult = list.getLast();

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getLastShouldThrow() {
        CustomList<String> list = getPrepopulatedCustomLinkedList(Collections.emptyList());

        assertThrowsExactly(NoSuchElementException.class, list::getLast);
    }

    @Test
    void removeFirstPositive() {
        String expectedResult = "El_1";
        CustomList<String> list = getPrepopulatedCustomLinkedList(List.of(expectedResult, "El_2"));
        int initSize = list.size();

        String actualResult = list.removeFirst();

        assertEquals(expectedResult, actualResult);
        assertEquals(initSize - 1, list.size());
    }

    @Test
    void removeFirstShouldThrow() {
        CustomList<String> list = getPrepopulatedCustomLinkedList(Collections.emptyList());

        assertThrowsExactly(NoSuchElementException.class, list::removeFirst);
    }

    @Test
    void removeLastPositive() {
        String expectedResult = "El_2";
        CustomList<String> list = getPrepopulatedCustomLinkedList(List.of("El_1", expectedResult));
        int initSize = list.size();

        String actualResult = list.removeLast();

        assertEquals(expectedResult, actualResult);
        assertEquals(initSize - 1, list.size());
    }

    @Test
    void removeLastShouldThrow() {
        CustomList<String> list = getPrepopulatedCustomLinkedList(Collections.emptyList());

        assertThrowsExactly(NoSuchElementException.class, list::removeLast);
    }

    @ParameterizedTest
    @MethodSource("checkRemoveByIndex")
    void removeByIndexPositive(List<String> initList, int index, String expectedElement, int expectedSize) {
        CustomList<String> list = getPrepopulatedCustomLinkedList(initList);

        String removed = list.remove(index);

        assertEquals(expectedSize, list.size());
        assertEquals(expectedElement, removed);
    }

    @ParameterizedTest
    @MethodSource("checkRemoveByIndexThrows")
    void removeByIndexShouldThrow(List<String> initList, int index) {
        CustomList<String> list = getPrepopulatedCustomLinkedList(initList);

        assertThrowsExactly(IllegalArgumentException.class, () -> list.remove(index));
    }
}