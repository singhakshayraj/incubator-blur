/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.nearinfinity.blur.thrift.generated;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum RowMutationType implements TEnum {
  DELETE(0),
  REPLACE(1),
  UPDATE(2);

  private final int value;

  private RowMutationType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static RowMutationType findByValue(int value) { 
    switch (value) {
      case 0:
        return DELETE;
      case 1:
        return REPLACE;
      case 2:
        return UPDATE;
      default:
        return null;
    }
  }
}
