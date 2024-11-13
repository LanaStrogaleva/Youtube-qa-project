package api;

import dev.failsafe.internal.util.Assert;
import groovy.transform.ASTTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReqresTest {
    private final static String URI = "https://reqres.in";
    /**
     * 1. Получить список пользователей со второй страница на сайте https://reqres.in/
     * 2. Убедиться что id пользователей содержаться в их avatar;
     * 3. Убедиться, что email пользователей имеет окончание reqres.in;
     */
    @Test
    public void CheckAvatarAndIdTest() {
        List<UserData> users = given()
                .when()
                .contentType(ContentType.JSON)
                .get(URI + "/api/users?page=2")
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

}
