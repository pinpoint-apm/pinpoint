/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.navercorp.pinpoint.thrift.dto.command;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
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
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2016-9-12")
public class TCmdActiveThreadDumpRes implements org.apache.thrift.TBase<TCmdActiveThreadDumpRes, TCmdActiveThreadDumpRes._Fields>, java.io.Serializable, Cloneable, Comparable<TCmdActiveThreadDumpRes> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TCmdActiveThreadDumpRes");

  private static final org.apache.thrift.protocol.TField THREAD_DUMPS_FIELD_DESC = new org.apache.thrift.protocol.TField("threadDumps", org.apache.thrift.protocol.TType.LIST, (short)1);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TCmdActiveThreadDumpResStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TCmdActiveThreadDumpResTupleSchemeFactory());
  }

  private List<TActiveThreadDump> threadDumps; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    THREAD_DUMPS((short)1, "threadDumps");

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
        case 1: // THREAD_DUMPS
          return THREAD_DUMPS;
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
    tmpMap.put(_Fields.THREAD_DUMPS, new org.apache.thrift.meta_data.FieldMetaData("threadDumps", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TActiveThreadDump.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TCmdActiveThreadDumpRes.class, metaDataMap);
  }

  public TCmdActiveThreadDumpRes() {
  }

  public TCmdActiveThreadDumpRes(
    List<TActiveThreadDump> threadDumps)
  {
    this();
    this.threadDumps = threadDumps;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TCmdActiveThreadDumpRes(TCmdActiveThreadDumpRes other) {
    if (other.isSetThreadDumps()) {
      List<TActiveThreadDump> __this__threadDumps = new ArrayList<TActiveThreadDump>(other.threadDumps.size());
      for (TActiveThreadDump other_element : other.threadDumps) {
        __this__threadDumps.add(new TActiveThreadDump(other_element));
      }
      this.threadDumps = __this__threadDumps;
    }
  }

  public TCmdActiveThreadDumpRes deepCopy() {
    return new TCmdActiveThreadDumpRes(this);
  }

  @Override
  public void clear() {
    this.threadDumps = null;
  }

  public int getThreadDumpsSize() {
    return (this.threadDumps == null) ? 0 : this.threadDumps.size();
  }

  public java.util.Iterator<TActiveThreadDump> getThreadDumpsIterator() {
    return (this.threadDumps == null) ? null : this.threadDumps.iterator();
  }

  public void addToThreadDumps(TActiveThreadDump elem) {
    if (this.threadDumps == null) {
      this.threadDumps = new ArrayList<TActiveThreadDump>();
    }
    this.threadDumps.add(elem);
  }

  public List<TActiveThreadDump> getThreadDumps() {
    return this.threadDumps;
  }

  public void setThreadDumps(List<TActiveThreadDump> threadDumps) {
    this.threadDumps = threadDumps;
  }

  public void unsetThreadDumps() {
    this.threadDumps = null;
  }

  /** Returns true if field threadDumps is set (has been assigned a value) and false otherwise */
  public boolean isSetThreadDumps() {
    return this.threadDumps != null;
  }

  public void setThreadDumpsIsSet(boolean value) {
    if (!value) {
      this.threadDumps = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case THREAD_DUMPS:
      if (value == null) {
        unsetThreadDumps();
      } else {
        setThreadDumps((List<TActiveThreadDump>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case THREAD_DUMPS:
      return getThreadDumps();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case THREAD_DUMPS:
      return isSetThreadDumps();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TCmdActiveThreadDumpRes)
      return this.equals((TCmdActiveThreadDumpRes)that);
    return false;
  }

  public boolean equals(TCmdActiveThreadDumpRes that) {
    if (that == null)
      return false;

    boolean this_present_threadDumps = true && this.isSetThreadDumps();
    boolean that_present_threadDumps = true && that.isSetThreadDumps();
    if (this_present_threadDumps || that_present_threadDumps) {
      if (!(this_present_threadDumps && that_present_threadDumps))
        return false;
      if (!this.threadDumps.equals(that.threadDumps))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_threadDumps = true && (isSetThreadDumps());
    list.add(present_threadDumps);
    if (present_threadDumps)
      list.add(threadDumps);

    return list.hashCode();
  }

  @Override
  public int compareTo(TCmdActiveThreadDumpRes other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetThreadDumps()).compareTo(other.isSetThreadDumps());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetThreadDumps()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.threadDumps, other.threadDumps);
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
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TCmdActiveThreadDumpRes(");
    boolean first = true;

    sb.append("threadDumps:");
    if (this.threadDumps == null) {
      sb.append("null");
    } else {
      sb.append(this.threadDumps);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
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

  private static class TCmdActiveThreadDumpResStandardSchemeFactory implements SchemeFactory {
    public TCmdActiveThreadDumpResStandardScheme getScheme() {
      return new TCmdActiveThreadDumpResStandardScheme();
    }
  }

  private static class TCmdActiveThreadDumpResStandardScheme extends StandardScheme<TCmdActiveThreadDumpRes> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TCmdActiveThreadDumpRes struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // THREAD_DUMPS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list40 = iprot.readListBegin();
                struct.threadDumps = new ArrayList<TActiveThreadDump>(_list40.size);
                TActiveThreadDump _elem41;
                for (int _i42 = 0; _i42 < _list40.size; ++_i42)
                {
                  _elem41 = new TActiveThreadDump();
                  _elem41.read(iprot);
                  struct.threadDumps.add(_elem41);
                }
                iprot.readListEnd();
              }
              struct.setThreadDumpsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TCmdActiveThreadDumpRes struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.threadDumps != null) {
        oprot.writeFieldBegin(THREAD_DUMPS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.threadDumps.size()));
          for (TActiveThreadDump _iter43 : struct.threadDumps)
          {
            _iter43.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TCmdActiveThreadDumpResTupleSchemeFactory implements SchemeFactory {
    public TCmdActiveThreadDumpResTupleScheme getScheme() {
      return new TCmdActiveThreadDumpResTupleScheme();
    }
  }

  private static class TCmdActiveThreadDumpResTupleScheme extends TupleScheme<TCmdActiveThreadDumpRes> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TCmdActiveThreadDumpRes struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetThreadDumps()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetThreadDumps()) {
        {
          oprot.writeI32(struct.threadDumps.size());
          for (TActiveThreadDump _iter44 : struct.threadDumps)
          {
            _iter44.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TCmdActiveThreadDumpRes struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list45 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.threadDumps = new ArrayList<TActiveThreadDump>(_list45.size);
          TActiveThreadDump _elem46;
          for (int _i47 = 0; _i47 < _list45.size; ++_i47)
          {
            _elem46 = new TActiveThreadDump();
            _elem46.read(iprot);
            struct.threadDumps.add(_elem46);
          }
        }
        struct.setThreadDumpsIsSet(true);
      }
    }
  }

}

