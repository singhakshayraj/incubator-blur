/**
 * Autogenerated by Thrift Compiler (0.7.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.nearinfinity.blur.thrift.generated;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchRecordResult implements org.apache.thrift.TBase<FetchRecordResult, FetchRecordResult._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("FetchRecordResult");

  private static final org.apache.thrift.protocol.TField ROWID_FIELD_DESC = new org.apache.thrift.protocol.TField("rowid", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField RECORD_FIELD_DESC = new org.apache.thrift.protocol.TField("record", org.apache.thrift.protocol.TType.STRUCT, (short)2);

  public String rowid; // required
  public Record record; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ROWID((short)1, "rowid"),
    RECORD((short)2, "record");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ROWID
          return ROWID;
        case 2: // RECORD
          return RECORD;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ROWID, new org.apache.thrift.meta_data.FieldMetaData("rowid", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.RECORD, new org.apache.thrift.meta_data.FieldMetaData("record", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Record.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(FetchRecordResult.class, metaDataMap);
  }

  public FetchRecordResult() {
  }

  public FetchRecordResult(
    String rowid,
    Record record)
  {
    this();
    this.rowid = rowid;
    this.record = record;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public FetchRecordResult(FetchRecordResult other) {
    if (other.isSetRowid()) {
      this.rowid = other.rowid;
    }
    if (other.isSetRecord()) {
      this.record = new Record(other.record);
    }
  }

  public FetchRecordResult deepCopy() {
    return new FetchRecordResult(this);
  }

  @Override
  public void clear() {
    this.rowid = null;
    this.record = null;
  }

  public String getRowid() {
    return this.rowid;
  }

  public FetchRecordResult setRowid(String rowid) {
    this.rowid = rowid;
    return this;
  }

  public void unsetRowid() {
    this.rowid = null;
  }

  /** Returns true if field rowid is set (has been assigned a value) and false otherwise */
  public boolean isSetRowid() {
    return this.rowid != null;
  }

  public void setRowidIsSet(boolean value) {
    if (!value) {
      this.rowid = null;
    }
  }

  public Record getRecord() {
    return this.record;
  }

  public FetchRecordResult setRecord(Record record) {
    this.record = record;
    return this;
  }

  public void unsetRecord() {
    this.record = null;
  }

  /** Returns true if field record is set (has been assigned a value) and false otherwise */
  public boolean isSetRecord() {
    return this.record != null;
  }

  public void setRecordIsSet(boolean value) {
    if (!value) {
      this.record = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ROWID:
      if (value == null) {
        unsetRowid();
      } else {
        setRowid((String)value);
      }
      break;

    case RECORD:
      if (value == null) {
        unsetRecord();
      } else {
        setRecord((Record)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ROWID:
      return getRowid();

    case RECORD:
      return getRecord();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ROWID:
      return isSetRowid();
    case RECORD:
      return isSetRecord();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof FetchRecordResult)
      return this.equals((FetchRecordResult)that);
    return false;
  }

  public boolean equals(FetchRecordResult that) {
    if (that == null)
      return false;

    boolean this_present_rowid = true && this.isSetRowid();
    boolean that_present_rowid = true && that.isSetRowid();
    if (this_present_rowid || that_present_rowid) {
      if (!(this_present_rowid && that_present_rowid))
        return false;
      if (!this.rowid.equals(that.rowid))
        return false;
    }

    boolean this_present_record = true && this.isSetRecord();
    boolean that_present_record = true && that.isSetRecord();
    if (this_present_record || that_present_record) {
      if (!(this_present_record && that_present_record))
        return false;
      if (!this.record.equals(that.record))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(FetchRecordResult other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    FetchRecordResult typedOther = (FetchRecordResult)other;

    lastComparison = Boolean.valueOf(isSetRowid()).compareTo(typedOther.isSetRowid());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRowid()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.rowid, typedOther.rowid);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetRecord()).compareTo(typedOther.isSetRecord());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetRecord()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.record, typedOther.record);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    org.apache.thrift.protocol.TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == org.apache.thrift.protocol.TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // ROWID
          if (field.type == org.apache.thrift.protocol.TType.STRING) {
            this.rowid = iprot.readString();
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // RECORD
          if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
            this.record = new Record();
            this.record.read(iprot);
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.rowid != null) {
      oprot.writeFieldBegin(ROWID_FIELD_DESC);
      oprot.writeString(this.rowid);
      oprot.writeFieldEnd();
    }
    if (this.record != null) {
      oprot.writeFieldBegin(RECORD_FIELD_DESC);
      this.record.write(oprot);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("FetchRecordResult(");
    boolean first = true;

    sb.append("rowid:");
    if (this.rowid == null) {
      sb.append("null");
    } else {
      sb.append(this.rowid);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("record:");
    if (this.record == null) {
      sb.append("null");
    } else {
      sb.append(this.record);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

}

