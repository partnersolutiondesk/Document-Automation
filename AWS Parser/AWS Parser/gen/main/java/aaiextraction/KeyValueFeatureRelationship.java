// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: aaiextraction/dto/extraction.proto

package aaiextraction;

/**
 * Protobuf type {@code aaiextraction.KeyValueFeatureRelationship}
 */
public final class KeyValueFeatureRelationship extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:aaiextraction.KeyValueFeatureRelationship)
    KeyValueFeatureRelationshipOrBuilder {
private static final long serialVersionUID = 0L;
  // Use KeyValueFeatureRelationship.newBuilder() to construct.
  private KeyValueFeatureRelationship(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private KeyValueFeatureRelationship() {
    key_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    value_ = com.google.protobuf.LazyStringArrayList.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new KeyValueFeatureRelationship();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private KeyValueFeatureRelationship(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              key_ = new com.google.protobuf.LazyStringArrayList();
              mutable_bitField0_ |= 0x00000001;
            }
            key_.add(s);
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();
            if (!((mutable_bitField0_ & 0x00000002) != 0)) {
              value_ = new com.google.protobuf.LazyStringArrayList();
              mutable_bitField0_ |= 0x00000002;
            }
            value_.add(s);
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        key_ = key_.getUnmodifiableView();
      }
      if (((mutable_bitField0_ & 0x00000002) != 0)) {
        value_ = value_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return aaiextraction.Extraction.internal_static_aaiextraction_KeyValueFeatureRelationship_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return aaiextraction.Extraction.internal_static_aaiextraction_KeyValueFeatureRelationship_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            aaiextraction.KeyValueFeatureRelationship.class, aaiextraction.KeyValueFeatureRelationship.Builder.class);
  }

  public static final int KEY_FIELD_NUMBER = 1;
  private com.google.protobuf.LazyStringList key_;
  /**
   * <code>repeated string key = 1;</code>
   * @return A list containing the key.
   */
  public com.google.protobuf.ProtocolStringList
      getKeyList() {
    return key_;
  }
  /**
   * <code>repeated string key = 1;</code>
   * @return The count of key.
   */
  public int getKeyCount() {
    return key_.size();
  }
  /**
   * <code>repeated string key = 1;</code>
   * @param index The index of the element to return.
   * @return The key at the given index.
   */
  public java.lang.String getKey(int index) {
    return key_.get(index);
  }
  /**
   * <code>repeated string key = 1;</code>
   * @param index The index of the value to return.
   * @return The bytes of the key at the given index.
   */
  public com.google.protobuf.ByteString
      getKeyBytes(int index) {
    return key_.getByteString(index);
  }

  public static final int VALUE_FIELD_NUMBER = 2;
  private com.google.protobuf.LazyStringList value_;
  /**
   * <code>repeated string value = 2;</code>
   * @return A list containing the value.
   */
  public com.google.protobuf.ProtocolStringList
      getValueList() {
    return value_;
  }
  /**
   * <code>repeated string value = 2;</code>
   * @return The count of value.
   */
  public int getValueCount() {
    return value_.size();
  }
  /**
   * <code>repeated string value = 2;</code>
   * @param index The index of the element to return.
   * @return The value at the given index.
   */
  public java.lang.String getValue(int index) {
    return value_.get(index);
  }
  /**
   * <code>repeated string value = 2;</code>
   * @param index The index of the value to return.
   * @return The bytes of the value at the given index.
   */
  public com.google.protobuf.ByteString
      getValueBytes(int index) {
    return value_.getByteString(index);
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    for (int i = 0; i < key_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, key_.getRaw(i));
    }
    for (int i = 0; i < value_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, value_.getRaw(i));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      for (int i = 0; i < key_.size(); i++) {
        dataSize += computeStringSizeNoTag(key_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getKeyList().size();
    }
    {
      int dataSize = 0;
      for (int i = 0; i < value_.size(); i++) {
        dataSize += computeStringSizeNoTag(value_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getValueList().size();
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof aaiextraction.KeyValueFeatureRelationship)) {
      return super.equals(obj);
    }
    aaiextraction.KeyValueFeatureRelationship other = (aaiextraction.KeyValueFeatureRelationship) obj;

    if (!getKeyList()
        .equals(other.getKeyList())) return false;
    if (!getValueList()
        .equals(other.getValueList())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (getKeyCount() > 0) {
      hash = (37 * hash) + KEY_FIELD_NUMBER;
      hash = (53 * hash) + getKeyList().hashCode();
    }
    if (getValueCount() > 0) {
      hash = (37 * hash) + VALUE_FIELD_NUMBER;
      hash = (53 * hash) + getValueList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static aaiextraction.KeyValueFeatureRelationship parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(aaiextraction.KeyValueFeatureRelationship prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code aaiextraction.KeyValueFeatureRelationship}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:aaiextraction.KeyValueFeatureRelationship)
      aaiextraction.KeyValueFeatureRelationshipOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return aaiextraction.Extraction.internal_static_aaiextraction_KeyValueFeatureRelationship_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return aaiextraction.Extraction.internal_static_aaiextraction_KeyValueFeatureRelationship_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              aaiextraction.KeyValueFeatureRelationship.class, aaiextraction.KeyValueFeatureRelationship.Builder.class);
    }

    // Construct using aaiextraction.KeyValueFeatureRelationship.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      key_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      value_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000002);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return aaiextraction.Extraction.internal_static_aaiextraction_KeyValueFeatureRelationship_descriptor;
    }

    @java.lang.Override
    public aaiextraction.KeyValueFeatureRelationship getDefaultInstanceForType() {
      return aaiextraction.KeyValueFeatureRelationship.getDefaultInstance();
    }

    @java.lang.Override
    public aaiextraction.KeyValueFeatureRelationship build() {
      aaiextraction.KeyValueFeatureRelationship result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public aaiextraction.KeyValueFeatureRelationship buildPartial() {
      aaiextraction.KeyValueFeatureRelationship result = new aaiextraction.KeyValueFeatureRelationship(this);
      int from_bitField0_ = bitField0_;
      if (((bitField0_ & 0x00000001) != 0)) {
        key_ = key_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.key_ = key_;
      if (((bitField0_ & 0x00000002) != 0)) {
        value_ = value_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000002);
      }
      result.value_ = value_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof aaiextraction.KeyValueFeatureRelationship) {
        return mergeFrom((aaiextraction.KeyValueFeatureRelationship)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(aaiextraction.KeyValueFeatureRelationship other) {
      if (other == aaiextraction.KeyValueFeatureRelationship.getDefaultInstance()) return this;
      if (!other.key_.isEmpty()) {
        if (key_.isEmpty()) {
          key_ = other.key_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureKeyIsMutable();
          key_.addAll(other.key_);
        }
        onChanged();
      }
      if (!other.value_.isEmpty()) {
        if (value_.isEmpty()) {
          value_ = other.value_;
          bitField0_ = (bitField0_ & ~0x00000002);
        } else {
          ensureValueIsMutable();
          value_.addAll(other.value_);
        }
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      aaiextraction.KeyValueFeatureRelationship parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (aaiextraction.KeyValueFeatureRelationship) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private com.google.protobuf.LazyStringList key_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    private void ensureKeyIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        key_ = new com.google.protobuf.LazyStringArrayList(key_);
        bitField0_ |= 0x00000001;
       }
    }
    /**
     * <code>repeated string key = 1;</code>
     * @return A list containing the key.
     */
    public com.google.protobuf.ProtocolStringList
        getKeyList() {
      return key_.getUnmodifiableView();
    }
    /**
     * <code>repeated string key = 1;</code>
     * @return The count of key.
     */
    public int getKeyCount() {
      return key_.size();
    }
    /**
     * <code>repeated string key = 1;</code>
     * @param index The index of the element to return.
     * @return The key at the given index.
     */
    public java.lang.String getKey(int index) {
      return key_.get(index);
    }
    /**
     * <code>repeated string key = 1;</code>
     * @param index The index of the value to return.
     * @return The bytes of the key at the given index.
     */
    public com.google.protobuf.ByteString
        getKeyBytes(int index) {
      return key_.getByteString(index);
    }
    /**
     * <code>repeated string key = 1;</code>
     * @param index The index to set the value at.
     * @param value The key to set.
     * @return This builder for chaining.
     */
    public Builder setKey(
        int index, java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureKeyIsMutable();
      key_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string key = 1;</code>
     * @param value The key to add.
     * @return This builder for chaining.
     */
    public Builder addKey(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureKeyIsMutable();
      key_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string key = 1;</code>
     * @param values The key to add.
     * @return This builder for chaining.
     */
    public Builder addAllKey(
        java.lang.Iterable<java.lang.String> values) {
      ensureKeyIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, key_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string key = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearKey() {
      key_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string key = 1;</code>
     * @param value The bytes of the key to add.
     * @return This builder for chaining.
     */
    public Builder addKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      ensureKeyIsMutable();
      key_.add(value);
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringList value_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    private void ensureValueIsMutable() {
      if (!((bitField0_ & 0x00000002) != 0)) {
        value_ = new com.google.protobuf.LazyStringArrayList(value_);
        bitField0_ |= 0x00000002;
       }
    }
    /**
     * <code>repeated string value = 2;</code>
     * @return A list containing the value.
     */
    public com.google.protobuf.ProtocolStringList
        getValueList() {
      return value_.getUnmodifiableView();
    }
    /**
     * <code>repeated string value = 2;</code>
     * @return The count of value.
     */
    public int getValueCount() {
      return value_.size();
    }
    /**
     * <code>repeated string value = 2;</code>
     * @param index The index of the element to return.
     * @return The value at the given index.
     */
    public java.lang.String getValue(int index) {
      return value_.get(index);
    }
    /**
     * <code>repeated string value = 2;</code>
     * @param index The index of the value to return.
     * @return The bytes of the value at the given index.
     */
    public com.google.protobuf.ByteString
        getValueBytes(int index) {
      return value_.getByteString(index);
    }
    /**
     * <code>repeated string value = 2;</code>
     * @param index The index to set the value at.
     * @param value The value to set.
     * @return This builder for chaining.
     */
    public Builder setValue(
        int index, java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureValueIsMutable();
      value_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string value = 2;</code>
     * @param value The value to add.
     * @return This builder for chaining.
     */
    public Builder addValue(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureValueIsMutable();
      value_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string value = 2;</code>
     * @param values The value to add.
     * @return This builder for chaining.
     */
    public Builder addAllValue(
        java.lang.Iterable<java.lang.String> values) {
      ensureValueIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, value_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string value = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearValue() {
      value_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string value = 2;</code>
     * @param value The bytes of the value to add.
     * @return This builder for chaining.
     */
    public Builder addValueBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      ensureValueIsMutable();
      value_.add(value);
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:aaiextraction.KeyValueFeatureRelationship)
  }

  // @@protoc_insertion_point(class_scope:aaiextraction.KeyValueFeatureRelationship)
  private static final aaiextraction.KeyValueFeatureRelationship DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new aaiextraction.KeyValueFeatureRelationship();
  }

  public static aaiextraction.KeyValueFeatureRelationship getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<KeyValueFeatureRelationship>
      PARSER = new com.google.protobuf.AbstractParser<KeyValueFeatureRelationship>() {
    @java.lang.Override
    public KeyValueFeatureRelationship parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new KeyValueFeatureRelationship(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<KeyValueFeatureRelationship> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<KeyValueFeatureRelationship> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public aaiextraction.KeyValueFeatureRelationship getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
