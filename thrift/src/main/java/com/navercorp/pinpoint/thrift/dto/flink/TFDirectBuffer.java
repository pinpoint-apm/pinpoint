/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.navercorp.pinpoint.thrift.dto.flink;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2018-03-23")
public class TFDirectBuffer implements org.apache.thrift.TBase<TFDirectBuffer, TFDirectBuffer._Fields>, java.io.Serializable, Cloneable, Comparable<TFDirectBuffer> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TFDirectBuffer");

  private static final org.apache.thrift.protocol.TField DIRECT_COUNT_FIELD_DESC = new org.apache.thrift.protocol.TField("directCount", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField DIRECT_MEMORY_USED_FIELD_DESC = new org.apache.thrift.protocol.TField("directMemoryUsed", org.apache.thrift.protocol.TType.I64, (short)2);
  private static final org.apache.thrift.protocol.TField MAPPED_COUNT_FIELD_DESC = new org.apache.thrift.protocol.TField("mappedCount", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField MAPPED_MEMORY_USED_FIELD_DESC = new org.apache.thrift.protocol.TField("mappedMemoryUsed", org.apache.thrift.protocol.TType.I64, (short)4);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TFDirectBufferStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TFDirectBufferTupleSchemeFactory();

  private long directCount; // optional
  private long directMemoryUsed; // optional
  private long mappedCount; // optional
  private long mappedMemoryUsed; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    DIRECT_COUNT((short)1, "directCount"),
    DIRECT_MEMORY_USED((short)2, "directMemoryUsed"),
    MAPPED_COUNT((short)3, "mappedCount"),
    MAPPED_MEMORY_USED((short)4, "mappedMemoryUsed");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // DIRECT_COUNT
          return DIRECT_COUNT;
        case 2: // DIRECT_MEMORY_USED
          return DIRECT_MEMORY_USED;
        case 3: // MAPPED_COUNT
          return MAPPED_COUNT;
        case 4: // MAPPED_MEMORY_USED
          return MAPPED_MEMORY_USED;
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
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __DIRECTCOUNT_ISSET_ID = 0;
  private static final int __DIRECTMEMORYUSED_ISSET_ID = 1;
  private static final int __MAPPEDCOUNT_ISSET_ID = 2;
  private static final int __MAPPEDMEMORYUSED_ISSET_ID = 3;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.DIRECT_COUNT,_Fields.DIRECT_MEMORY_USED,_Fields.MAPPED_COUNT,_Fields.MAPPED_MEMORY_USED};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.DIRECT_COUNT, new org.apache.thrift.meta_data.FieldMetaData("directCount", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.DIRECT_MEMORY_USED, new org.apache.thrift.meta_data.FieldMetaData("directMemoryUsed", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.MAPPED_COUNT, new org.apache.thrift.meta_data.FieldMetaData("mappedCount", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.MAPPED_MEMORY_USED, new org.apache.thrift.meta_data.FieldMetaData("mappedMemoryUsed", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TFDirectBuffer.class, metaDataMap);
  }

  public TFDirectBuffer() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TFDirectBuffer(TFDirectBuffer other) {
    __isset_bitfield = other.__isset_bitfield;
    this.directCount = other.directCount;
    this.directMemoryUsed = other.directMemoryUsed;
    this.mappedCount = other.mappedCount;
    this.mappedMemoryUsed = other.mappedMemoryUsed;
  }

  public TFDirectBuffer deepCopy() {
    return new TFDirectBuffer(this);
  }

  @Override
  public void clear() {
    setDirectCountIsSet(false);
    this.directCount = 0;
    setDirectMemoryUsedIsSet(false);
    this.directMemoryUsed = 0;
    setMappedCountIsSet(false);
    this.mappedCount = 0;
    setMappedMemoryUsedIsSet(false);
    this.mappedMemoryUsed = 0;
  }

  public long getDirectCount() {
    return this.directCount;
  }

  public void setDirectCount(long directCount) {
    this.directCount = directCount;
    setDirectCountIsSet(true);
  }

  public void unsetDirectCount() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __DIRECTCOUNT_ISSET_ID);
  }

  /** Returns true if field directCount is set (has been assigned a value) and false otherwise */
  public boolean isSetDirectCount() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __DIRECTCOUNT_ISSET_ID);
  }

  public void setDirectCountIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __DIRECTCOUNT_ISSET_ID, value);
  }

  public long getDirectMemoryUsed() {
    return this.directMemoryUsed;
  }

  public void setDirectMemoryUsed(long directMemoryUsed) {
    this.directMemoryUsed = directMemoryUsed;
    setDirectMemoryUsedIsSet(true);
  }

  public void unsetDirectMemoryUsed() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __DIRECTMEMORYUSED_ISSET_ID);
  }

  /** Returns true if field directMemoryUsed is set (has been assigned a value) and false otherwise */
  public boolean isSetDirectMemoryUsed() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __DIRECTMEMORYUSED_ISSET_ID);
  }

  public void setDirectMemoryUsedIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __DIRECTMEMORYUSED_ISSET_ID, value);
  }

  public long getMappedCount() {
    return this.mappedCount;
  }

  public void setMappedCount(long mappedCount) {
    this.mappedCount = mappedCount;
    setMappedCountIsSet(true);
  }

  public void unsetMappedCount() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MAPPEDCOUNT_ISSET_ID);
  }

  /** Returns true if field mappedCount is set (has been assigned a value) and false otherwise */
  public boolean isSetMappedCount() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MAPPEDCOUNT_ISSET_ID);
  }

  public void setMappedCountIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MAPPEDCOUNT_ISSET_ID, value);
  }

  public long getMappedMemoryUsed() {
    return this.mappedMemoryUsed;
  }

  public void setMappedMemoryUsed(long mappedMemoryUsed) {
    this.mappedMemoryUsed = mappedMemoryUsed;
    setMappedMemoryUsedIsSet(true);
  }

  public void unsetMappedMemoryUsed() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __MAPPEDMEMORYUSED_ISSET_ID);
  }

  /** Returns true if field mappedMemoryUsed is set (has been assigned a value) and false otherwise */
  public boolean isSetMappedMemoryUsed() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __MAPPEDMEMORYUSED_ISSET_ID);
  }

  public void setMappedMemoryUsedIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __MAPPEDMEMORYUSED_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case DIRECT_COUNT:
      if (value == null) {
        unsetDirectCount();
      } else {
        setDirectCount((java.lang.Long)value);
      }
      break;

    case DIRECT_MEMORY_USED:
      if (value == null) {
        unsetDirectMemoryUsed();
      } else {
        setDirectMemoryUsed((java.lang.Long)value);
      }
      break;

    case MAPPED_COUNT:
      if (value == null) {
        unsetMappedCount();
      } else {
        setMappedCount((java.lang.Long)value);
      }
      break;

    case MAPPED_MEMORY_USED:
      if (value == null) {
        unsetMappedMemoryUsed();
      } else {
        setMappedMemoryUsed((java.lang.Long)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case DIRECT_COUNT:
      return getDirectCount();

    case DIRECT_MEMORY_USED:
      return getDirectMemoryUsed();

    case MAPPED_COUNT:
      return getMappedCount();

    case MAPPED_MEMORY_USED:
      return getMappedMemoryUsed();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case DIRECT_COUNT:
      return isSetDirectCount();
    case DIRECT_MEMORY_USED:
      return isSetDirectMemoryUsed();
    case MAPPED_COUNT:
      return isSetMappedCount();
    case MAPPED_MEMORY_USED:
      return isSetMappedMemoryUsed();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof TFDirectBuffer)
      return this.equals((TFDirectBuffer)that);
    return false;
  }

  public boolean equals(TFDirectBuffer that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_directCount = true && this.isSetDirectCount();
    boolean that_present_directCount = true && that.isSetDirectCount();
    if (this_present_directCount || that_present_directCount) {
      if (!(this_present_directCount && that_present_directCount))
        return false;
      if (this.directCount != that.directCount)
        return false;
    }

    boolean this_present_directMemoryUsed = true && this.isSetDirectMemoryUsed();
    boolean that_present_directMemoryUsed = true && that.isSetDirectMemoryUsed();
    if (this_present_directMemoryUsed || that_present_directMemoryUsed) {
      if (!(this_present_directMemoryUsed && that_present_directMemoryUsed))
        return false;
      if (this.directMemoryUsed != that.directMemoryUsed)
        return false;
    }

    boolean this_present_mappedCount = true && this.isSetMappedCount();
    boolean that_present_mappedCount = true && that.isSetMappedCount();
    if (this_present_mappedCount || that_present_mappedCount) {
      if (!(this_present_mappedCount && that_present_mappedCount))
        return false;
      if (this.mappedCount != that.mappedCount)
        return false;
    }

    boolean this_present_mappedMemoryUsed = true && this.isSetMappedMemoryUsed();
    boolean that_present_mappedMemoryUsed = true && that.isSetMappedMemoryUsed();
    if (this_present_mappedMemoryUsed || that_present_mappedMemoryUsed) {
      if (!(this_present_mappedMemoryUsed && that_present_mappedMemoryUsed))
        return false;
      if (this.mappedMemoryUsed != that.mappedMemoryUsed)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetDirectCount()) ? 131071 : 524287);
    if (isSetDirectCount())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(directCount);

    hashCode = hashCode * 8191 + ((isSetDirectMemoryUsed()) ? 131071 : 524287);
    if (isSetDirectMemoryUsed())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(directMemoryUsed);

    hashCode = hashCode * 8191 + ((isSetMappedCount()) ? 131071 : 524287);
    if (isSetMappedCount())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(mappedCount);

    hashCode = hashCode * 8191 + ((isSetMappedMemoryUsed()) ? 131071 : 524287);
    if (isSetMappedMemoryUsed())
      hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(mappedMemoryUsed);

    return hashCode;
  }

  @Override
  public int compareTo(TFDirectBuffer other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetDirectCount()).compareTo(other.isSetDirectCount());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDirectCount()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.directCount, other.directCount);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetDirectMemoryUsed()).compareTo(other.isSetDirectMemoryUsed());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDirectMemoryUsed()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.directMemoryUsed, other.directMemoryUsed);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMappedCount()).compareTo(other.isSetMappedCount());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMappedCount()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.mappedCount, other.mappedCount);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMappedMemoryUsed()).compareTo(other.isSetMappedMemoryUsed());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMappedMemoryUsed()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.mappedMemoryUsed, other.mappedMemoryUsed);
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
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("TFDirectBuffer(");
    boolean first = true;

    if (isSetDirectCount()) {
      sb.append("directCount:");
      sb.append(this.directCount);
      first = false;
    }
    if (isSetDirectMemoryUsed()) {
      if (!first) sb.append(", ");
      sb.append("directMemoryUsed:");
      sb.append(this.directMemoryUsed);
      first = false;
    }
    if (isSetMappedCount()) {
      if (!first) sb.append(", ");
      sb.append("mappedCount:");
      sb.append(this.mappedCount);
      first = false;
    }
    if (isSetMappedMemoryUsed()) {
      if (!first) sb.append(", ");
      sb.append("mappedMemoryUsed:");
      sb.append(this.mappedMemoryUsed);
      first = false;
    }
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

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TFDirectBufferStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TFDirectBufferStandardScheme getScheme() {
      return new TFDirectBufferStandardScheme();
    }
  }

  private static class TFDirectBufferStandardScheme extends org.apache.thrift.scheme.StandardScheme<TFDirectBuffer> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TFDirectBuffer struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // DIRECT_COUNT
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.directCount = iprot.readI64();
              struct.setDirectCountIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DIRECT_MEMORY_USED
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.directMemoryUsed = iprot.readI64();
              struct.setDirectMemoryUsedIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // MAPPED_COUNT
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.mappedCount = iprot.readI64();
              struct.setMappedCountIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // MAPPED_MEMORY_USED
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.mappedMemoryUsed = iprot.readI64();
              struct.setMappedMemoryUsedIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TFDirectBuffer struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.isSetDirectCount()) {
        oprot.writeFieldBegin(DIRECT_COUNT_FIELD_DESC);
        oprot.writeI64(struct.directCount);
        oprot.writeFieldEnd();
      }
      if (struct.isSetDirectMemoryUsed()) {
        oprot.writeFieldBegin(DIRECT_MEMORY_USED_FIELD_DESC);
        oprot.writeI64(struct.directMemoryUsed);
        oprot.writeFieldEnd();
      }
      if (struct.isSetMappedCount()) {
        oprot.writeFieldBegin(MAPPED_COUNT_FIELD_DESC);
        oprot.writeI64(struct.mappedCount);
        oprot.writeFieldEnd();
      }
      if (struct.isSetMappedMemoryUsed()) {
        oprot.writeFieldBegin(MAPPED_MEMORY_USED_FIELD_DESC);
        oprot.writeI64(struct.mappedMemoryUsed);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TFDirectBufferTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TFDirectBufferTupleScheme getScheme() {
      return new TFDirectBufferTupleScheme();
    }
  }

  private static class TFDirectBufferTupleScheme extends org.apache.thrift.scheme.TupleScheme<TFDirectBuffer> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TFDirectBuffer struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetDirectCount()) {
        optionals.set(0);
      }
      if (struct.isSetDirectMemoryUsed()) {
        optionals.set(1);
      }
      if (struct.isSetMappedCount()) {
        optionals.set(2);
      }
      if (struct.isSetMappedMemoryUsed()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetDirectCount()) {
        oprot.writeI64(struct.directCount);
      }
      if (struct.isSetDirectMemoryUsed()) {
        oprot.writeI64(struct.directMemoryUsed);
      }
      if (struct.isSetMappedCount()) {
        oprot.writeI64(struct.mappedCount);
      }
      if (struct.isSetMappedMemoryUsed()) {
        oprot.writeI64(struct.mappedMemoryUsed);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TFDirectBuffer struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.directCount = iprot.readI64();
        struct.setDirectCountIsSet(true);
      }
      if (incoming.get(1)) {
        struct.directMemoryUsed = iprot.readI64();
        struct.setDirectMemoryUsedIsSet(true);
      }
      if (incoming.get(2)) {
        struct.mappedCount = iprot.readI64();
        struct.setMappedCountIsSet(true);
      }
      if (incoming.get(3)) {
        struct.mappedMemoryUsed = iprot.readI64();
        struct.setMappedMemoryUsedIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

