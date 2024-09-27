package org.rudi.facet.rva.impl.rva.bean;

import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * FullAddressesResponse
 */
public class FullAddressesResponse {

	private FullAddressesResponseRva rva;

	public FullAddressesResponse rva(FullAddressesResponseRva rva) {
		this.rva = rva;
		return this;
	}

	/**
	 * Get rva
	 * 
	 * @return rva
	 */
	@Valid
	@Schema(name = "rva", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@JsonProperty("rva")
	public FullAddressesResponseRva getRva() {
		return rva;
	}

	public void setRva(FullAddressesResponseRva rva) {
		this.rva = rva;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FullAddressesResponse fullAddressesResponse = (FullAddressesResponse) o;
		return Objects.equals(this.rva, fullAddressesResponse.rva);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rva);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class FullAddressesResponse {\n");
		sb.append("    rva: ").append(toIndentedString(rva)).append("\n");
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
