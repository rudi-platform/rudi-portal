package org.rudi.facet.rva.impl.rva.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * FullAddressesAnswer
 */
public class FullAddressesAnswer {

	@Valid
	private List<org.rudi.facet.rva.impl.rva.bean.Address> addresses;

	private Status status;

	public FullAddressesAnswer addresses(List<org.rudi.facet.rva.impl.rva.bean.Address> addresses) {
		this.addresses = addresses;
		return this;
	}

	public FullAddressesAnswer addAddressesItem(Address addressesItem) {
		if (this.addresses == null) {
			this.addresses = new ArrayList<>();
		}
		this.addresses.add(addressesItem);
		return this;
	}

	/**
	 * Get addresses
	 * 
	 * @return addresses
	 */
	@Valid
	@Schema(name = "addresses", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("addresses")
	public List<org.rudi.facet.rva.impl.rva.bean.Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<org.rudi.facet.rva.impl.rva.bean.Address> addresses) {
		this.addresses = addresses;
	}

	public FullAddressesAnswer status(Status status) {
		this.status = status;
		return this;
	}

	/**
	 * Get status
	 * 
	 * @return status
	 */
	@Valid
	@Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("status")
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FullAddressesAnswer fullAddressesAnswer = (FullAddressesAnswer) o;
		return Objects.equals(this.addresses, fullAddressesAnswer.addresses)
				&& Objects.equals(this.status, fullAddressesAnswer.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(addresses, status);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FullAddressesAnswer {\n");
		sb.append("    addresses: ").append(toIndentedString(addresses)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
