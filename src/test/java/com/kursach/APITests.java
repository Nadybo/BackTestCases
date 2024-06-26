package com.kursach;

import com.kursach.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertEquals;
import io.restassured.module.jsv.JsonSchemaValidator;

public class APITests extends BaseTest {

    @BeforeClass
    public static void setUo(){
        BaseTest.setUp();
        requestSpecification.basePath("/users");
    }

    @Test
    public void checkListUsersValidate() {
        sendGetRequest(200)
                .assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("UsersSchema.json"));
    }

    @Test
    public void checkListUsersDTO() {
        UsersPage usersPage = checkStatusCodeGet("https://reqres.in/api/users?page=2", 200)
                    .extract().as(UsersPage.class);
        Assert.assertEquals(usersPage.getPage().intValue(), 2);
        Assert.assertEquals(usersPage.getPer_page().intValue(), 6);
        Assert.assertEquals(usersPage.getTotal().intValue(), 12);
        Assert.assertEquals(usersPage.getTotal_pages().intValue(), 2);
        Assert.assertFalse(usersPage.getData().isEmpty());
        Assert.assertEquals(usersPage.getData().get(2).getFirst_name(), "Tobias");
        Assert.assertEquals(usersPage.getData().get(2).getLast_name(), "Funke");
    }

    @Test
    public void testUserFromResponse() {
        UserData userData = checkStatusCodeGet("https://reqres.in/api/users/2", 200)
                .extract().body().jsonPath().getObject("data", UserData.class);
        assertThat("Идентификатор пользователя должен быть равен 2", userData.getId(), is(2));
        assertThat("Адрес электронной почты пользователя должен быть janet.weaver@reqres.in", userData.getEmail(), is("janet.weaver@reqres.in"));
        assertThat("Имя пользователя должно быть Джанет", userData.getFirst_name(), is("Janet"));
        assertThat("Фамилия пользователя должна быть Weaver", userData.getLast_name(), is("Weaver"));
        assertThat("Аватар пользователя должен быть корректным", userData.getAvatar(), is("https://reqres.in/img/faces/2-image.jpg"));
    }

    @Test
    public void userNotFound() {
        String responseBody = checkStatusCodeGet("https://reqres.in/api/users/22", 404)
                        .extract()
                        .asString();
        assertEquals("{}", responseBody, "Expected response body to be empty JSON object {}");
    }

    @Test
    public void createUser(){
        People people = new People("aslbek", "student");
        PeopleCreater peopleCreater = checkStatusCodePost(people,"https://reqres.in/api/users",201)
                .extract()
                .as(PeopleCreater.class);
        assertEquals(peopleCreater.getName(),people.getName());
        assertEquals(peopleCreater.getJob(), people.getJob());
    }


}
