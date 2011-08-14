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

public class BlurQueryStatus implements org.apache.thrift.TBase<BlurQueryStatus, BlurQueryStatus._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("BlurQueryStatus");

  private static final org.apache.thrift.protocol.TField QUERY_FIELD_DESC = new org.apache.thrift.protocol.TField("query", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField CPU_TIMES_FIELD_DESC = new org.apache.thrift.protocol.TField("cpuTimes", org.apache.thrift.protocol.TType.MAP, (short)2);
  private static final org.apache.thrift.protocol.TField COMPLETE_SHARDS_FIELD_DESC = new org.apache.thrift.protocol.TField("completeShards", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField TOTAL_SHARDS_FIELD_DESC = new org.apache.thrift.protocol.TField("totalShards", org.apache.thrift.protocol.TType.I32, (short)4);
  private static final org.apache.thrift.protocol.TField STATE_FIELD_DESC = new org.apache.thrift.protocol.TField("state", org.apache.thrift.protocol.TType.I32, (short)5);
  private static final org.apache.thrift.protocol.TField UUID_FIELD_DESC = new org.apache.thrift.protocol.TField("uuid", org.apache.thrift.protocol.TType.I64, (short)6);

  public BlurQuery query; // required
  public Map<String,CpuTime> cpuTimes; // required
  public int completeShards; // required
  public int totalShards; // required
  /**
   * 
   * @see QueryState
   */
  public QueryState state; // required
  public long uuid; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    QUERY((short)1, "query"),
    CPU_TIMES((short)2, "cpuTimes"),
    COMPLETE_SHARDS((short)3, "completeShards"),
    TOTAL_SHARDS((short)4, "totalShards"),
    /**
     * 
     * @see QueryState
     */
    STATE((short)5, "state"),
    UUID((short)6, "uuid");

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
        case 1: // QUERY
          return QUERY;
        case 2: // CPU_TIMES
          return CPU_TIMES;
        case 3: // COMPLETE_SHARDS
          return COMPLETE_SHARDS;
        case 4: // TOTAL_SHARDS
          return TOTAL_SHARDS;
        case 5: // STATE
          return STATE;
        case 6: // UUID
          return UUID;
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
  private static final int __COMPLETESHARDS_ISSET_ID = 0;
  private static final int __TOTALSHARDS_ISSET_ID = 1;
  private static final int __UUID_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);

  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.QUERY, new org.apache.thrift.meta_data.FieldMetaData("query", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, BlurQuery.class)));
    tmpMap.put(_Fields.CPU_TIMES, new org.apache.thrift.meta_data.FieldMetaData("cpuTimes", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, CpuTime.class))));
    tmpMap.put(_Fields.COMPLETE_SHARDS, new org.apache.thrift.meta_data.FieldMetaData("completeShards", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.TOTAL_SHARDS, new org.apache.thrift.meta_data.FieldMetaData("totalShards", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.STATE, new org.apache.thrift.meta_data.FieldMetaData("state", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, QueryState.class)));
    tmpMap.put(_Fields.UUID, new org.apache.thrift.meta_data.FieldMetaData("uuid", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(BlurQueryStatus.class, metaDataMap);
  }

  public BlurQueryStatus() {
  }

  public BlurQueryStatus(
    BlurQuery query,
    Map<String,CpuTime> cpuTimes,
    int completeShards,
    int totalShards,
    QueryState state,
    long uuid)
  {
    this();
    this.query = query;
    this.cpuTimes = cpuTimes;
    this.completeShards = completeShards;
    setCompleteShardsIsSet(true);
    this.totalShards = totalShards;
    setTotalShardsIsSet(true);
    this.state = state;
    this.uuid = uuid;
    setUuidIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public BlurQueryStatus(BlurQueryStatus other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetQuery()) {
      this.query = new BlurQuery(other.query);
    }
    if (other.isSetCpuTimes()) {
      Map<String,CpuTime> __this__cpuTimes = new HashMap<String,CpuTime>();
      for (Map.Entry<String, CpuTime> other_element : other.cpuTimes.entrySet()) {

        String other_element_key = other_element.getKey();
        CpuTime other_element_value = other_element.getValue();

        String __this__cpuTimes_copy_key = other_element_key;

        CpuTime __this__cpuTimes_copy_value = new CpuTime(other_element_value);

        __this__cpuTimes.put(__this__cpuTimes_copy_key, __this__cpuTimes_copy_value);
      }
      this.cpuTimes = __this__cpuTimes;
    }
    this.completeShards = other.completeShards;
    this.totalShards = other.totalShards;
    if (other.isSetState()) {
      this.state = other.state;
    }
    this.uuid = other.uuid;
  }

  public BlurQueryStatus deepCopy() {
    return new BlurQueryStatus(this);
  }

  @Override
  public void clear() {
    this.query = null;
    this.cpuTimes = null;
    setCompleteShardsIsSet(false);
    this.completeShards = 0;
    setTotalShardsIsSet(false);
    this.totalShards = 0;
    this.state = null;
    setUuidIsSet(false);
    this.uuid = 0;
  }

  public BlurQuery getQuery() {
    return this.query;
  }

  public BlurQueryStatus setQuery(BlurQuery query) {
    this.query = query;
    return this;
  }

  public void unsetQuery() {
    this.query = null;
  }

  /** Returns true if field query is set (has been assigned a value) and false otherwise */
  public boolean isSetQuery() {
    return this.query != null;
  }

  public void setQueryIsSet(boolean value) {
    if (!value) {
      this.query = null;
    }
  }

  public int getCpuTimesSize() {
    return (this.cpuTimes == null) ? 0 : this.cpuTimes.size();
  }

  public void putToCpuTimes(String key, CpuTime val) {
    if (this.cpuTimes == null) {
      this.cpuTimes = new HashMap<String,CpuTime>();
    }
    this.cpuTimes.put(key, val);
  }

  public Map<String,CpuTime> getCpuTimes() {
    return this.cpuTimes;
  }

  public BlurQueryStatus setCpuTimes(Map<String,CpuTime> cpuTimes) {
    this.cpuTimes = cpuTimes;
    return this;
  }

  public void unsetCpuTimes() {
    this.cpuTimes = null;
  }

  /** Returns true if field cpuTimes is set (has been assigned a value) and false otherwise */
  public boolean isSetCpuTimes() {
    return this.cpuTimes != null;
  }

  public void setCpuTimesIsSet(boolean value) {
    if (!value) {
      this.cpuTimes = null;
    }
  }

  public int getCompleteShards() {
    return this.completeShards;
  }

  public BlurQueryStatus setCompleteShards(int completeShards) {
    this.completeShards = completeShards;
    setCompleteShardsIsSet(true);
    return this;
  }

  public void unsetCompleteShards() {
    __isset_bit_vector.clear(__COMPLETESHARDS_ISSET_ID);
  }

  /** Returns true if field completeShards is set (has been assigned a value) and false otherwise */
  public boolean isSetCompleteShards() {
    return __isset_bit_vector.get(__COMPLETESHARDS_ISSET_ID);
  }

  public void setCompleteShardsIsSet(boolean value) {
    __isset_bit_vector.set(__COMPLETESHARDS_ISSET_ID, value);
  }

  public int getTotalShards() {
    return this.totalShards;
  }

  public BlurQueryStatus setTotalShards(int totalShards) {
    this.totalShards = totalShards;
    setTotalShardsIsSet(true);
    return this;
  }

  public void unsetTotalShards() {
    __isset_bit_vector.clear(__TOTALSHARDS_ISSET_ID);
  }

  /** Returns true if field totalShards is set (has been assigned a value) and false otherwise */
  public boolean isSetTotalShards() {
    return __isset_bit_vector.get(__TOTALSHARDS_ISSET_ID);
  }

  public void setTotalShardsIsSet(boolean value) {
    __isset_bit_vector.set(__TOTALSHARDS_ISSET_ID, value);
  }

  /**
   * 
   * @see QueryState
   */
  public QueryState getState() {
    return this.state;
  }

  /**
   * 
   * @see QueryState
   */
  public BlurQueryStatus setState(QueryState state) {
    this.state = state;
    return this;
  }

  public void unsetState() {
    this.state = null;
  }

  /** Returns true if field state is set (has been assigned a value) and false otherwise */
  public boolean isSetState() {
    return this.state != null;
  }

  public void setStateIsSet(boolean value) {
    if (!value) {
      this.state = null;
    }
  }

  public long getUuid() {
    return this.uuid;
  }

  public BlurQueryStatus setUuid(long uuid) {
    this.uuid = uuid;
    setUuidIsSet(true);
    return this;
  }

  public void unsetUuid() {
    __isset_bit_vector.clear(__UUID_ISSET_ID);
  }

  /** Returns true if field uuid is set (has been assigned a value) and false otherwise */
  public boolean isSetUuid() {
    return __isset_bit_vector.get(__UUID_ISSET_ID);
  }

  public void setUuidIsSet(boolean value) {
    __isset_bit_vector.set(__UUID_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case QUERY:
      if (value == null) {
        unsetQuery();
      } else {
        setQuery((BlurQuery)value);
      }
      break;

    case CPU_TIMES:
      if (value == null) {
        unsetCpuTimes();
      } else {
        setCpuTimes((Map<String,CpuTime>)value);
      }
      break;

    case COMPLETE_SHARDS:
      if (value == null) {
        unsetCompleteShards();
      } else {
        setCompleteShards((Integer)value);
      }
      break;

    case TOTAL_SHARDS:
      if (value == null) {
        unsetTotalShards();
      } else {
        setTotalShards((Integer)value);
      }
      break;

    case STATE:
      if (value == null) {
        unsetState();
      } else {
        setState((QueryState)value);
      }
      break;

    case UUID:
      if (value == null) {
        unsetUuid();
      } else {
        setUuid((Long)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case QUERY:
      return getQuery();

    case CPU_TIMES:
      return getCpuTimes();

    case COMPLETE_SHARDS:
      return Integer.valueOf(getCompleteShards());

    case TOTAL_SHARDS:
      return Integer.valueOf(getTotalShards());

    case STATE:
      return getState();

    case UUID:
      return Long.valueOf(getUuid());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case QUERY:
      return isSetQuery();
    case CPU_TIMES:
      return isSetCpuTimes();
    case COMPLETE_SHARDS:
      return isSetCompleteShards();
    case TOTAL_SHARDS:
      return isSetTotalShards();
    case STATE:
      return isSetState();
    case UUID:
      return isSetUuid();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof BlurQueryStatus)
      return this.equals((BlurQueryStatus)that);
    return false;
  }

  public boolean equals(BlurQueryStatus that) {
    if (that == null)
      return false;

    boolean this_present_query = true && this.isSetQuery();
    boolean that_present_query = true && that.isSetQuery();
    if (this_present_query || that_present_query) {
      if (!(this_present_query && that_present_query))
        return false;
      if (!this.query.equals(that.query))
        return false;
    }

    boolean this_present_cpuTimes = true && this.isSetCpuTimes();
    boolean that_present_cpuTimes = true && that.isSetCpuTimes();
    if (this_present_cpuTimes || that_present_cpuTimes) {
      if (!(this_present_cpuTimes && that_present_cpuTimes))
        return false;
      if (!this.cpuTimes.equals(that.cpuTimes))
        return false;
    }

    boolean this_present_completeShards = true;
    boolean that_present_completeShards = true;
    if (this_present_completeShards || that_present_completeShards) {
      if (!(this_present_completeShards && that_present_completeShards))
        return false;
      if (this.completeShards != that.completeShards)
        return false;
    }

    boolean this_present_totalShards = true;
    boolean that_present_totalShards = true;
    if (this_present_totalShards || that_present_totalShards) {
      if (!(this_present_totalShards && that_present_totalShards))
        return false;
      if (this.totalShards != that.totalShards)
        return false;
    }

    boolean this_present_state = true && this.isSetState();
    boolean that_present_state = true && that.isSetState();
    if (this_present_state || that_present_state) {
      if (!(this_present_state && that_present_state))
        return false;
      if (!this.state.equals(that.state))
        return false;
    }

    boolean this_present_uuid = true;
    boolean that_present_uuid = true;
    if (this_present_uuid || that_present_uuid) {
      if (!(this_present_uuid && that_present_uuid))
        return false;
      if (this.uuid != that.uuid)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(BlurQueryStatus other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    BlurQueryStatus typedOther = (BlurQueryStatus)other;

    lastComparison = Boolean.valueOf(isSetQuery()).compareTo(typedOther.isSetQuery());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetQuery()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.query, typedOther.query);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCpuTimes()).compareTo(typedOther.isSetCpuTimes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCpuTimes()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cpuTimes, typedOther.cpuTimes);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetCompleteShards()).compareTo(typedOther.isSetCompleteShards());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCompleteShards()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.completeShards, typedOther.completeShards);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetTotalShards()).compareTo(typedOther.isSetTotalShards());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTotalShards()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.totalShards, typedOther.totalShards);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetState()).compareTo(typedOther.isSetState());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetState()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.state, typedOther.state);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetUuid()).compareTo(typedOther.isSetUuid());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetUuid()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.uuid, typedOther.uuid);
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
        case 1: // QUERY
          if (field.type == org.apache.thrift.protocol.TType.STRUCT) {
            this.query = new BlurQuery();
            this.query.read(iprot);
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // CPU_TIMES
          if (field.type == org.apache.thrift.protocol.TType.MAP) {
            {
              org.apache.thrift.protocol.TMap _map57 = iprot.readMapBegin();
              this.cpuTimes = new HashMap<String,CpuTime>(2*_map57.size);
              for (int _i58 = 0; _i58 < _map57.size; ++_i58)
              {
                String _key59; // required
                CpuTime _val60; // required
                _key59 = iprot.readString();
                _val60 = new CpuTime();
                _val60.read(iprot);
                this.cpuTimes.put(_key59, _val60);
              }
              iprot.readMapEnd();
            }
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // COMPLETE_SHARDS
          if (field.type == org.apache.thrift.protocol.TType.I32) {
            this.completeShards = iprot.readI32();
            setCompleteShardsIsSet(true);
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 4: // TOTAL_SHARDS
          if (field.type == org.apache.thrift.protocol.TType.I32) {
            this.totalShards = iprot.readI32();
            setTotalShardsIsSet(true);
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 5: // STATE
          if (field.type == org.apache.thrift.protocol.TType.I32) {
            this.state = QueryState.findByValue(iprot.readI32());
          } else { 
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 6: // UUID
          if (field.type == org.apache.thrift.protocol.TType.I64) {
            this.uuid = iprot.readI64();
            setUuidIsSet(true);
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
    if (this.query != null) {
      oprot.writeFieldBegin(QUERY_FIELD_DESC);
      this.query.write(oprot);
      oprot.writeFieldEnd();
    }
    if (this.cpuTimes != null) {
      oprot.writeFieldBegin(CPU_TIMES_FIELD_DESC);
      {
        oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRUCT, this.cpuTimes.size()));
        for (Map.Entry<String, CpuTime> _iter61 : this.cpuTimes.entrySet())
        {
          oprot.writeString(_iter61.getKey());
          _iter61.getValue().write(oprot);
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldBegin(COMPLETE_SHARDS_FIELD_DESC);
    oprot.writeI32(this.completeShards);
    oprot.writeFieldEnd();
    oprot.writeFieldBegin(TOTAL_SHARDS_FIELD_DESC);
    oprot.writeI32(this.totalShards);
    oprot.writeFieldEnd();
    if (this.state != null) {
      oprot.writeFieldBegin(STATE_FIELD_DESC);
      oprot.writeI32(this.state.getValue());
      oprot.writeFieldEnd();
    }
    oprot.writeFieldBegin(UUID_FIELD_DESC);
    oprot.writeI64(this.uuid);
    oprot.writeFieldEnd();
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("BlurQueryStatus(");
    boolean first = true;

    sb.append("query:");
    if (this.query == null) {
      sb.append("null");
    } else {
      sb.append(this.query);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("cpuTimes:");
    if (this.cpuTimes == null) {
      sb.append("null");
    } else {
      sb.append(this.cpuTimes);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("completeShards:");
    sb.append(this.completeShards);
    first = false;
    if (!first) sb.append(", ");
    sb.append("totalShards:");
    sb.append(this.totalShards);
    first = false;
    if (!first) sb.append(", ");
    sb.append("state:");
    if (this.state == null) {
      sb.append("null");
    } else {
      sb.append(this.state);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("uuid:");
    sb.append(this.uuid);
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

}

