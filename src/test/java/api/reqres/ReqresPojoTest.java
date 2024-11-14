package api.reqres;

import api.reqres.colors.ColorsData;
import api.reqres.registration.Register;
import api.reqres.registration.SuccessReg;
import api.reqres.registration.UnSuccessReg;
import api.reqres.spec.Specification;
import api.reqres.users.UserData;
import api.reqres.users.UserTime;
import api.reqres.users.UserTimeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Апи тесты с Pojo классами")
//@Feature("Api Regres Pojo")

public class ReqresPojoTest {
    private final static String URI = "https://reqres.in/";
    /**
     * 1. Получить список пользователей со второй страница на сайте https://reqres.in/
     * 2. Убедиться что id пользователей содержаться в их avatar;
     * 3. Убедиться, что email пользователей имеет окончание reqres.in;
     */
    @DisplayName("Аватары содержат айди пользователей")
    @Test
    public void CheckAvatarAndIdTest() {
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecOK200());
        //1 способ сравнивать значения напрямую из экземпляров класса
        List<UserData> users = given()
                .when()
                .get("/api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);
        //проверка аватар содержит айди
        users.forEach(x -> assertTrue(x.getAvatar().contains(x.getId().toString())));
        //проверка почты оканчиваются на reqres.in
        assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));

        //2 способ сравнивать значения через получения списков
        //список с аватарками
        List<String> avatars = users.stream()
                .map(UserData::getAvatar)
                .collect(Collectors.toList());
        //список с айди
        List<String> ids = users.stream()
                .map(x -> x.getId().toString())
                .collect(Collectors.toList());
        //проверка через сравнение двух списков
        for (int i = 0; i < avatars.size(); i++) {
            assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }
    /**
     * 1. Используя сервис https://reqres.in/ протестировать регистрацию пользователя в системе
     * 2. Тест для успешной регистрации
     */
    @DisplayName("Успешная регистрация")
    @Test
    public void successRegTest() {
        Integer expectID = 4;
        String expectToken = "QpwL5tke4Pnpja7X4";
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecOK200());
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(SuccessReg.class);

        assertNotNull(successReg.getId());
        assertNotNull(successReg.getToken());

        assertEquals(expectID, successReg.getId());
        assertEquals(expectToken, successReg.getToken());

    }

    /**
     * 1. Используя сервис https://reqres.in/ протестировать регистрацию пользователя в системе
     * 2. Тест для неуспешной регистрации (не введен пароль)
     */
    @DisplayName("Не успешная регистрация")
    @Test
    public void unSuccessRegTest() {
        String expectError = "Missing password";
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecError400());
        Register user = new Register("sydney@fife", "");
        UnSuccessReg unSuccessReg = given()
                .body(user)
                .when()
                .post("/api/register")
                .then().log().all()
                .extract().as(UnSuccessReg.class);
        assertEquals(expectError, unSuccessReg.getError());
    }

    /**
     * Используя сервис https://reqres.in/ убедиться, что операция LIST<RESOURCE> возвращает данные,
     * отсортированные по годам.
     */
    @DisplayName("Года правильно отсортированы")
    @Test
    public void sortedYearTest() {
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecOK200());
        List<ColorsData> colorsData = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().jsonPath().getList("data", ColorsData.class);
        List<Integer> years = colorsData.stream().map(ColorsData::getYear).toList();
        List<Integer> sortedYears = years.stream().sorted().toList();
        assertEquals(sortedYears,years);
    }

    /**
     * Тест 4.1
     * Используя сервис https://reqres.in/ попробовать удалить второго пользователя и сравнить статус-код
     */
    @DisplayName("Удаление пользователя")
    @Test
    public void deleteUserTest() {
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecUniqCode(204));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();
    }

    /**
     * Используя сервис https://reqres.in/ обновить информацию о пользователе и сравнить дату обновления с текущей датой на машине
     */
    @Test
    @DisplayName("Время сервера и компьютера совпадают")
    public void timeTest() {
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecOK200());
        UserTime userTime = new UserTime("mortheus", "zion rezident");
        UserTimeResponse response = given()
                .body(userTime)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);

        //так как время считается в плоть до миллисекунд, необходимо убрать последние символы, чтобы время было одинаковое
        String regex = "(.{11})$";
        String regex1 = "(.{5})$";
        String currentTime = Clock.systemUTC().instant().toString();
        System.out.println(currentTime);
        System.out.println(currentTime.replaceAll(regex,""));
        assertEquals(currentTime.replaceAll(regex, ""),response.getUpdatedAt().replaceAll(regex1, ""));
        System.out.println(response.getUpdatedAt().replaceAll(regex1, ""));
    }
}
