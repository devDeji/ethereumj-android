package org.ethereum.db;

import org.ethereum.trie.SecureTrie;
import org.ethereum.trie.Trie;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.DataWord;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Mandeleil
 * @since 24.06.2014
 */
public class ContractDetailsImpl implements ContractDetails {

    private byte[] rlpEncoded;

    private List<DataWord> storageKeys = new ArrayList<>();
    private List<DataWord> storageValues = new ArrayList<>();

    private byte[] code = ByteUtil.EMPTY_BYTE_ARRAY;

    private boolean dirty = false;
    private boolean deleted = false;

    private Trie storageTrie = new SecureTrie(null);

    public ContractDetailsImpl() {
    }

    public ContractDetailsImpl(byte[] rlpCode) {
        decode(rlpCode);
    }

    public ContractDetailsImpl(Map<DataWord, DataWord> storage, byte[] code) {
    }

    @Override
    public void put(DataWord key, DataWord value) {

        if (value.equals(DataWord.ZERO)) {

            storageTrie.delete(key.getData());
            int index = storageKeys.indexOf(key);
            if (index != -1) {
                storageKeys.remove(index);
                storageValues.remove(index);
            }
        } else {

            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
            int index = storageKeys.indexOf(key);
            if (index != -1) {
                storageKeys.remove(index);
                storageValues.remove(index);
            }
            storageKeys.add(key);
            storageValues.add(value);
        }

        this.setDirty(true);
        this.rlpEncoded = null;
    }

    @Override
    public DataWord get(DataWord key) {

        if (storageKeys.size() == 0)
            return null;

        int foundIndex = storageKeys.indexOf(key);
        if (foundIndex != -1) {
            DataWord value = storageValues.get(foundIndex);
            return value.clone();
        } else
            return null;
    }

    @Override
    public byte[] getCode() {
        return code;
    }

    @Override
    public void setCode(byte[] code) {
        this.code = code;
        this.setDirty(true);
        this.rlpEncoded = null;
    }

    @Override
    public byte[] getStorageHash() {

        storageTrie = new SecureTrie(null);
        // calc the trie for root hash
        for (int i = 0; i < storageKeys.size(); ++i) {
            storageTrie.update(storageKeys.get(i).getData(), RLP
                    .encodeElement(storageValues.get(i).getNoLeadZeroesData()));
        }
        return storageTrie.getRootHash();
    }

    @Override
    public void decode(byte[] rlpCode) {
        RLPList data = RLP.decode2(rlpCode);
        RLPList rlpList = (RLPList) data.get(0);

        RLPList keys = (RLPList) rlpList.get(0);
        RLPList values = (RLPList) rlpList.get(1);
        RLPElement code = rlpList.get(2);

        if (keys.size() > 0) {
            storageKeys = new ArrayList<>();
            storageValues = new ArrayList<>();
        }

        for (Object key : keys) {
            RLPItem rlpItem = (RLPItem) key;
            storageKeys.add(new DataWord(rlpItem.getRLPData()));
        }

        for (Object value : values) {
            RLPItem rlpItem = (RLPItem) value;
            storageValues.add(new DataWord(rlpItem.getRLPData()));
        }

        for (int i = 0; i < keys.size(); ++i) {
            DataWord key = storageKeys.get(i);
            DataWord value = storageValues.get(i);
            storageTrie.update(key.getData(), RLP.encodeElement(value.getNoLeadZeroesData()));
        }

        this.code = (code.getRLPData() == null) ? ByteUtil.EMPTY_BYTE_ARRAY : code.getRLPData();
        this.rlpEncoded = rlpCode;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }


    @Override
    public byte[] getEncoded() {

        if (rlpEncoded == null) {

            int size = storageKeys == null ? 0 : storageKeys.size();

            byte[][] keys = new byte[size][];
            byte[][] values = new byte[size][];

            for (int i = 0; i < size; ++i) {
                DataWord key = storageKeys.get(i);
                keys[i] = RLP.encodeElement(key.getData());
            }
            for (int i = 0; i < size; ++i) {
                DataWord value = storageValues.get(i);
                values[i] = RLP.encodeElement(value.getNoLeadZeroesData());
            }

            byte[] rlpKeysList = RLP.encodeList(keys);
            byte[] rlpValuesList = RLP.encodeList(values);
            byte[] rlpCode = RLP.encodeElement(code);

            this.rlpEncoded = RLP.encodeList(rlpKeysList, rlpValuesList, rlpCode);
        }
        return rlpEncoded;
    }

    @Override
    public Map<DataWord, DataWord> getStorage() {
        Map<DataWord, DataWord> storage = new HashMap<>();
        for (int i = 0; storageKeys != null && i < storageKeys.size(); ++i) {
            storage.put(storageKeys.get(i), storageValues.get(i));
        }
        return Collections.unmodifiableMap(storage);
    }

    @Override
    public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {
        this.storageKeys = storageKeys;
        this.storageValues = storageValues;
    }

    @Override
    public void setStorage(Map<DataWord, DataWord> storage) {

        List<DataWord> keys = new ArrayList<>();
        keys.addAll(storage.keySet());

        List<DataWord> values = new ArrayList<>();
        for (DataWord key : keys) {

            DataWord value = storage.get(key);
            values.add(value);
        }

        this.storageKeys = keys;
        this.storageValues = values;
    }


    @Override
    public ContractDetails clone() {

        ContractDetailsImpl contractDetails = new ContractDetailsImpl();

        contractDetails.setCode(this.getCode());
        contractDetails.setStorage(new ArrayList<>(this.storageKeys),
                new ArrayList<>(this.storageValues));
        return contractDetails;
    }

    @Override
    public String toString() {

        String ret = "  Code: " + Hex.toHexString(code) + "\n";
        ret += "  Storage: " + getStorage().toString();

        return ret;
    }

}

