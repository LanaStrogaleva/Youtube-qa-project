package api.reqres;

import api.reqres.registration.Register;
import api.reqres.spec.Specification;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

public class ReqresNoPojoTest {
    private final static String URL = "https://reqres.in";
    @Test
    public void checkAvatarsNoPojoTest() {
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        Response response = given()
                .when()
                .get("/api/users?page=2")
                .then().log().all()
                .body("page", equalTo(2))
                .body("data.id",notNullValue())
                .body("data.email",notNullValue())
                .body("data.first_name",notNullValue())
                .body("data.last_name",notNullValue())
                .body("data.avatar",notNullValue())
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        List<String> emails = jsonPath.getList("data.email");
        List<Integer> ids = jsonPath.getList("data.id");
        List<String> avatars = jsonPath.getList("data.avatar");
        for (int i = 0; i < emails.size(); i++) {
            //проверка аватар содержит айди
            assertTrue(avatars.get(i).contains(ids.get(i).toString()));
            // 1 способ: проверка почты оканчиваются на reqres.in - проверка в цикле
            assertTrue(emails.get(i).endsWith("@reqres.in"));
        }
        // 2 способ: проверка почты оканчиваются на reqres.in - с помощью stream api
            assertTrue(emails.stream().allMatch(x -> x.endsWith("@reqres.in")));
    }
    @Test
    public void successRegNoPojoTest() {
        Integer expectID = 4;
        String expectToken = "QpwL5tke4Pnpja7X4";
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecOK200());
        Map<String, String> user = new HashMap<>();
        user.put("email", "eve.holt@reqres.in");
        user.put("password", "pistol");

        // 1 способ: проверка с помощью Matchers в body ответа - без использования Response
        given()
                .body(user)
                .when()
                .post("/api/register")
                .then()
                .body("id", equalTo(expectID))
                .body("token", equalTo(expectToken));

        // 2 способ: проверка через Response
        Response response = given()
                .body(user)
                .when()
                .post("/api/register")
                .then()
                .extract().response();
        Integer id = response.jsonPath().get("id");
        String token = response.jsonPath().get("token");

        assertEquals(expectID,id);
        assertEquals(expectToken, token);
    }

    @Test
    public void unSuccessRegNoPojoTest() {
        String expectError = "Missing password";
        Specification.installSpecification(Specification.requestSpec(URL), Specification.responseSpecError400());
        Map<String, String> user = new HashMap<>();
        user.put("email", "sydney@fife");
        user.put("password", "");

        given()
                .body(user)
                .when()
                .post("/api/register")
                .then().log().all()
                .body("error", equalTo(expectError));

        Response response = given()
                .body(user)
                .when()
                .post("/api/register");
        String actualError = response.getBody().path("error");
        assertEquals(expectError,actualError);
        System.out.println(actualError);
    }
}
