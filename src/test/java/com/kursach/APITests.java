package com.kursach;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class APITests {

    @Test
    public void task1() {
        RestAssured.baseURI = "https://reqres.in/api";
        UsersPage usersPage = given()
                .when()
                .get("/users?page=2")
                .then()
                .log().all()
                .statusCode(200)
                .extract().as(UsersPage.class);

        assertEquals(2, usersPage.getPage().intValue(), "Expected page to be 2");
        assertEquals(2, usersPage.getTotal_pages().intValue(), "Expected total_pages to be 2");
        assertEquals(12, usersPage.getTotal().intValue(), "Expected total to be 12");

        List<UserData> data = usersPage.getData();
        List<Integer> ids = data.stream().map(UserData::getId).collect(Collectors.toList());
        assertEquals(ids.size(), ids.stream().distinct().count(), "Expected IDs to be unique");

        boolean found = data.stream().anyMatch(d -> "Tobias".equals(d.getFirst_name()) && "Funke".equals(d.getLast_name()));
        assertTrue(found, "Expected to find a user with first_name=Tobias and last_name=Funke");
    }

    @Test
    public void userNotFound() {
        String responseBody =
                given()
                        .when()
                        .get("https://reqres.in/api/users/22")
                        .then()
                        .log().all()
                        .statusCode(404)
                        .extract()
                        .asString();

        assertEquals("{}", responseBody, "Expected response body to be empty JSON object {}");
    }

    @Test
    public void testUserFromResponse() {

        UserData userData = given()
                .when()
                .get("https://reqres.in/api/users/2")
                .then()
                .log().all()
                .statusCode(200)
                .extract().body().jsonPath().getObject("data", UserData.class);

        assertThat("User id should be 2", userData.getId(), is(2));
        assertThat("User email should be janet.weaver@reqres.in", userData.getEmail(), is("janet.weaver@reqres.in"));
        assertThat("User first name should be Janet", userData.getFirst_name(), is("Janet"));
        assertThat("User last name should be Weaver", userData.getLast_name(), is("Weaver"));
        assertThat("User avatar should be correct", userData.getAvatar(), is("https://reqres.in/img/faces/2-image.jpg"));
    }


    @Test
    public void createUser(){
        People people = new People("aslbek", "student");
        PeopleCreater peopleCreater = given()
                .contentType("application/json")
                .body(people)
                .when()
                .post("https://reqres.in/api/users")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(PeopleCreater.class);
        assertEquals(peopleCreater.getName(),people.getName());
        assertEquals(peopleCreater.getJob(), people.getJob());
    }
}
