package com.bookingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("passengers")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Passenger {

    @Id
    private String passengerId;
    private String bookingId; // fk -> booking
    private String name;
    private int age;
    private Gender gender;
    public String getPassengerId() {
		return passengerId;
	}
	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}
	public String getBookingId() {
		return bookingId;
	}
	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public Meal getMeal() {
		return meal;
	}
	public void setMeal(Meal meal) {
		this.meal = meal;
	}
	public String getSeatOutbound() {
		return seatOutbound;
	}
	public void setSeatOutbound(String seatOutbound) {
		this.seatOutbound = seatOutbound;
	}
	public String getSeatReturn() {
		return seatReturn;
	}
	public void setSeatReturn(String seatReturn) {
		this.seatReturn = seatReturn;
	}
	private Meal meal;
    private String seatOutbound; 
    private String seatReturn;   
}
