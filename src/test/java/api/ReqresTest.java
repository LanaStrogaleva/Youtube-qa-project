package api;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class ReqresTest {
    private final static String URI = "https://reqres.in";
    /**
     * 1. Получить список пользователей со второй страница на сайте https://reqres.in/
     * 2. Убедиться что id пользователей содержаться в их avatar;
     * 3. Убедиться, что email пользователей имеет окончание reqres.in;
     */
    @Test
    public void CheckAvatarAndIdTest() {
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecOK200());
        List<UserData> users = given()
                .when()
                .get("/api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);
        //проверка аватар содержит айди
        users.forEach(x -> assertTrue(x.getAvatar().contains(x.getId().toString())));
        //проверка почты оканчиваются на reqres.in
        assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));
        List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> ids = users.stream().map(x -> x.getId().toString()).collect(Collectors.toList());
        for (int i = 0; i < avatars.size(); i++) {
            assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }
    @Test
    public void successRegTest() {
        Integer expectID = 4;
        String expectToken = "QpwL5tke4Pnpja7X4";
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecOK200());
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("/api/register")
                .then().log().all()
                .extract().as(SuccessReg.class);

        assertNotNull(successReg.getId());
        assertNotNull(successReg.getToken());

        assertEquals(expectID, successReg.getId());
        assertEquals(expectToken, successReg.getToken());

    }
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
    @Test
    public void deleteUserTest() {
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecUniqCode(204));
        given()
                .when()
                .delete("api/users/2")
                .then().log().all();
    }
    @Test
    public void timeTest() {
        Specification.installSpecification(Specification.requestSpec(URI), Specification.responseSpecOK200());
        UserTime userTime = new UserTime("mortheus", "zion rezident");
        UserTimeResponse response = given()
                .body(userTime)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);
        String regex = "(.{11})$";
        String regex1 = "(.{5})$";
        String currentTime = Clock.systemUTC().instant().toString();
        System.out.println(currentTime);
        System.out.println(currentTime.replaceAll(regex,""));
        assertEquals(currentTime.replaceAll(regex, ""),response.getUpdatedAt().replaceAll(regex1, ""));
        System.out.println(response.getUpdatedAt().replaceAll(regex1, ""));
    }

}
