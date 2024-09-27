package org.rudi.facet.rva.impl.rva.bean;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Address
 */
public class Address {

	private String zipcode;

	private String insee;

	private Integer idaddress;

	private Integer idlane;

	private Integer number;

	private String extension;

	private String building;

	private String addr1;

	private String addr2;

	private String addr3;

	private String x;

	private String y;

	public Address zipcode(String zipcode) {
		this.zipcode = zipcode;
		return this;
	}

	/**
	 * Get zipcode
	 * 
	 * @return zipcode
	 */

	@Schema(name = "zipcode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("zipcode")
	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public Address insee(String insee) {
		this.insee = insee;
		return this;
	}

	/**
	 * Get insee
	 * 
	 * @return insee
	 */

	@Schema(name = "insee", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("insee")
	public String getInsee() {
		return insee;
	}

	public void setInsee(String insee) {
		this.insee = insee;
	}

	public Address idaddress(Integer idaddress) {
		this.idaddress = idaddress;
		return this;
	}

	/**
	 * Get idaddress
	 * 
	 * @return idaddress
	 */

	@Schema(name = "idaddress", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("idaddress")
	public Integer getIdaddress() {
		return idaddress;
	}

	public void setIdaddress(Integer idaddress) {
		this.idaddress = idaddress;
	}

	public Address idlane(Integer idlane) {
		this.idlane = idlane;
		return this;
	}

	/**
	 * Get idlane
	 * 
	 * @return idlane
	 */

	@Schema(name = "idlane", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("idlane")
	public Integer getIdlane() {
		return idlane;
	}

	public void setIdlane(Integer idlane) {
		this.idlane = idlane;
	}

	public Address number(Integer number) {
		this.number = number;
		return this;
	}

	/**
	 * Get number
	 * 
	 * @return number
	 */

	@Schema(name = "number", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("number")
	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Address extension(String extension) {
		this.extension = extension;
		return this;
	}

	/**
	 * Get extension
	 * 
	 * @return extension
	 */

	@Schema(name = "extension", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("extension")
	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public Address building(String building) {
		this.building = building;
		return this;
	}

	/**
	 * Get building
	 * 
	 * @return building
	 */

	@Schema(name = "building", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("building")
	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public Address addr1(String addr1) {
		this.addr1 = addr1;
		return this;
	}

	/**
	 * Get addr1
	 * 
	 * @return addr1
	 */

	@Schema(name = "addr1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("addr1")
	public String getAddr1() {
		return addr1;
	}

	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}

	public Address addr2(String addr2) {
		this.addr2 = addr2;
		return this;
	}

	/**
	 * Get addr2
	 * 
	 * @return addr2
	 */

	@Schema(name = "addr2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("addr2")
	public String getAddr2() {
		return addr2;
	}

	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}

	public Address addr3(String addr3) {
		this.addr3 = addr3;
		return this;
	}

	/**
	 * Get addr3
	 * 
	 * @return addr3
	 */

	@Schema(name = "addr3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("addr3")
	public String getAddr3() {
		return addr3;
	}

	public void setAddr3(String addr3) {
		this.addr3 = addr3;
	}

	public Address x(String x) {
		this.x = x;
		return this;
	}

	/**
	 * Get x
	 * 
	 * @return x
	 */

	@Schema(name = "x", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("x")
	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public Address y(String y) {
		this.y = y;
		return this;
	}

	/**
	 * Get y
	 * 
	 * @return y
	 */

	@Schema(name = "y", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("y")
	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Address address = (Address) o;
		return Objects.equals(this.zipcode, address.zipcode) && Objects.equals(this.insee, address.insee)
				&& Objects.equals(this.idaddress, address.idaddress) && Objects.equals(this.idlane, address.idlane)
				&& Objects.equals(this.number, address.number) && Objects.equals(this.extension, address.extension)
				&& Objects.equals(this.building, address.building) && Objects.equals(this.addr1, address.addr1)
				&& Objects.equals(this.addr2, address.addr2) && Objects.equals(this.addr3, address.addr3)
				&& Objects.equals(this.x, address.x) && Objects.equals(this.y, address.y);
	}

	@Override
	public int hashCode() {
		return Objects.hash(zipcode, insee, idaddress, idlane, number, extension, building, addr1, addr2, addr3, x, y);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Address {\n");
		sb.append("    zipcode: ").append(toIndentedString(zipcode)).append("\n");
		sb.append("    insee: ").append(toIndentedString(insee)).append("\n");
		sb.append("    idaddress: ").append(toIndentedString(idaddress)).append("\n");
		sb.append("    idlane: ").append(toIndentedString(idlane)).append("\n");
		sb.append("    number: ").append(toIndentedString(number)).append("\n");
		sb.append("    extension: ").append(toIndentedString(extension)).append("\n");
		sb.append("    building: ").append(toIndentedString(building)).append("\n");
		sb.append("    addr1: ").append(toIndentedString(addr1)).append("\n");
		sb.append("    addr2: ").append(toIndentedString(addr2)).append("\n");
		sb.append("    addr3: ").append(toIndentedString(addr3)).append("\n");
		sb.append("    x: ").append(toIndentedString(x)).append("\n");
		sb.append("    y: ").append(toIndentedString(y)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
