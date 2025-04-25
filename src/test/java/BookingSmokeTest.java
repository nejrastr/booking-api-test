
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingSmokeTest {

  static int bookingId;
  static String token;
  static final String BASE_URL = "https://restful-booker.herokuapp.com";

  @BeforeAll
  static void setup() {
    RestAssured.baseURI = BASE_URL;
  }

  @Test
  @Order(1)
  void createBooking() {
    String body = """
            {
                "firstname" : "Dino",
                "lastname" : "Merlin",
                "totalprice" : 1000,
                "depositpaid" : true,
                "bookingdates" : {
                    "checkin" : "2025-01-01",
                    "checkout" : "2025-01-11"
                },
                "additionalneeds" : "Breakfast"
            }
            """;

    Response response = given()
        .contentType("application/json")
        .body(body)
        .when()
        .post("/booking")
        .then()
        .statusCode(200)
        .body("booking.firstname", equalTo("Dino"))
        .extract().response();

    bookingId = response.path("bookingid");
    Assertions.assertTrue(bookingId > 0);
  }

  @Test
  @Order(2)
  void getBooking() {
    given()
        .when()
        .get("/booking/" + bookingId)
        .then()
        .statusCode(200)
        .body("firstname", equalTo("Dino"));
  }

  @Test
  @Order(3)
  void generateToken() {
    String credentials = """
            {
                "username": "admin",
                "password": "password123"
            }
            """;

    Response response = given()
        .contentType("application/json")
        .body(credentials)
        .when()
        .post("/auth")
        .then()
        .statusCode(200)
        .extract().response();

    token = response.path("token");
    Assertions.assertNotNull(token);
  }

  @Test
  @Order(4)
  void updateBooking() {
    String updateBody = """
            {
                "firstname" : "James",
                "lastname" : "Brown"
            }
            """;

    given()
        .contentType("application/json")
        .cookie("token", token)
        .body(updateBody)
        .when()
        .patch("/booking/" + bookingId)
        .then()
        .statusCode(200)
        .body("firstname", equalTo("James"))
        .body("lastname", equalTo("Brown"));
  }

  @Test
  @Order(5)
  void deleteBooking() {
    given()
        .cookie("token", token)
        .when()
        .delete("/booking/" + bookingId)
        .then()
        .statusCode(204);
  }
}

