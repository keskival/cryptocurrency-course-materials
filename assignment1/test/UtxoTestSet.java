// Copyright (C) 2016-2017 Enrique Albertos
// Distributed under the GNU GPL v2 software license

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UtxoTestSet represent a random created data test set for testing isValid and txHandler methods.
 * Creates an UtxPool ans several transactions, valid, invalid or conflicted
 * Several flags force different types of transactions
 * 
 * A UtxoTestSet can be
 * constructed wit by means of a builder:
 * 
 * <pre>
 * UtxoTestSet.builder()
 * 	.setPeopleSize(10)
 * 	.setUtxoTxNumber(10)
 * 	.setMaxUtxoTxOutput(10)
 * 	.setMaxValue(200)
 * 	.setTxPerTest(10)
 * 	.setMaxInput(10)
 * 	.setMaxOutput(10)
 * 	.setCorruptedPercentage(0)
 * 	.build();
 * </pre>
 * 
 * @author ealbertos
 *
 */
public class UtxoTestSet {
	
	/**
	 * Factory method that constructs a new builder of UtxoTestSet
	 * @return a new builder
	 */
	public static UtxoTestSetBuilder builder(){
		return new UtxoTestSetBuilder();
	}
	
	/**
	 * Validation list contains list with valid transactions, invalid transactions and conflicted transactions
	 * @return the validationLists
	 */
	public ValidationLists<Transaction> getValidationLists() {
		return new ValidationLists<>(validationLists);
	}
	
	/**
	 * Return the created Utxo Pool
	 * @return a copy of the pool
	 */
	public UTXOPool getUtxoPool() {
		return new UTXOPool(utxoPool);
	}

	
	/**
	 * Builder for UtxoTestSet
	 * @author ealbertos
	 *
	 */
	static class UtxoTestSetBuilder {
		private int peopleSize;
		private int utxoTxNumber;
		private int maxUtxoTxOutput;
		private double maxValue;
		private int txNumberPerTest;
		private int maxInputs;
		private int maxOutputs;
		private double corruptedPercentage = 0D;
		private boolean isForceInvalidPrivateKeys = false;
		private boolean isForceInvalidTotals = false;
		private boolean isClaimingOutputsNotInPool = false;
		private boolean isForceCorruptedSignature = false;
		private boolean isClaimingUtxoSeveralTimes = false;
		private boolean isForceNegativeOutputs = false;

		/**
		 * Number of different people address in the test set
		 * @param peopleNumber the number of different people address in the test set
		 * @return this builder
		 */
		public UtxoTestSetBuilder setPeopleSize(final int peopleNumber) {
			this.peopleSize = peopleNumber;
			return this;
		}

		/**
		 * Number of utxo in the pool to create for the set, the same number is created for the extraPool set
		 * Extra Pool set is used to create transactions tha use UTXO no in the actual pool
		 * @param utxoTxNumber number of utxo in the pool to create for the set
		 * @return this builder
		 */
		public UtxoTestSetBuilder setUtxoTxNumber(final int utxoTxNumber) {
			this.utxoTxNumber = utxoTxNumber;
			return this;
		}

		/**
		 * Max Number of utxo per output in trx created
		 * @param maxUtxoTxOutput
		 * @return this builder
		 */
		public UtxoTestSetBuilder setMaxUtxoTxOutput(final int maxUtxoTxOutput) {
			this.maxUtxoTxOutput = maxUtxoTxOutput;
			return this;
		}

		/**
		 * Max coin value per transaction, can be exceed in invalid transactions
		 * @param maxValue
		 * @return this builder
		 */
		public UtxoTestSetBuilder setMaxValue(final double maxValue) {
			this.maxValue = maxValue;
			return this;
		}

		/**
		 * number of tx per test
		 * @param txNumberPerTest number of tx per test
		 * @return this builder
		 */
		public UtxoTestSetBuilder setTxPerTest(final int txNumberPerTest) {
			this.txNumberPerTest = txNumberPerTest;
			return this;
		}

		/**
		 *  Max number of inputs
		 * @param maxInputs max number of inputs in transactions created
		 * @return
		 */
		public UtxoTestSetBuilder setMaxInput(int maxInputs) {
			this.maxInputs = maxInputs;
			return this;
		}

		/**
		 * set the max number of outputs in transactions created
		 * @param maxOutputs max number of outputs in transactions created
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setMaxOutput(int maxOutputs) {
			this.maxOutputs = maxOutputs;
			return this;
		}

		/**
		 * Set the percentage of corrupted transactions
		 * @param corruptedPercentage 0 to 1
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setCorruptedPercentage(double corruptedPercentage) {
			if(corruptedPercentage > 1 || corruptedPercentage < 0) {
				throw new IllegalArgumentException("Percentage value must be in the range (0-1)");
			}
			this.corruptedPercentage = corruptedPercentage;
			return this;
		}
		
		/**
		 * Create transactions containing signatures using incorrect private keys
		 * @param value
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setInvalidPrivateKeys(boolean value) {
			isForceInvalidPrivateKeys  = value;
			return this;
		}

		/**
		 * Create transactions whose total output value exceeds total input value
		 * @param value True force creation
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setInvalidTotals(boolean value) {
			isForceInvalidTotals  = value;
			return this;
		}

		/**
		 * Create transactions that claim outputs not in the current utxoPool
		 * @param value True force creation
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setClaimingOutputsNotInPool(boolean value) {
			isClaimingOutputsNotInPool  = value;
			return this;
		}
		
		/**
		 * Create transactions containing signatures of incorrect data
		 * @param value True force creation
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setForceCorruptedSignature(boolean value) {
			isForceCorruptedSignature  = value;
			return this;
		}

		/**
		 * Create transactions that claim the same UTXO multiple times
		 * @param value True force creation
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setClaimingUtxoSeveralTimes(boolean value) {
			isClaimingUtxoSeveralTimes  = value;
			return this;
		}
		
		/**
		 * Create transactions that contain a negative output value
		 * @param value True force creation
		 * @return  this builder
		 */
		public UtxoTestSetBuilder setNegativeOutputs(boolean value) {
			isForceNegativeOutputs  = value;
			return this;
		}

		public UtxoTestSet build() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
			return new UtxoTestSet(peopleSize,
					utxoTxNumber,
					maxUtxoTxOutput,
					maxValue,
					txNumberPerTest,
					maxInputs,
					maxOutputs,
					corruptedPercentage,
					isForceInvalidPrivateKeys,
					isForceInvalidTotals, 
					isClaimingOutputsNotInPool,
					isForceCorruptedSignature,
					isClaimingUtxoSeveralTimes,
					isForceNegativeOutputs);
		}


	}
	
	private final UTXOPool utxoPool;
	private final List<KeyPair> people;
	private final Map<UTXO, KeyPair> utxoToKeyPair;

	private final UTXOPool utxoExtraPool;
	private final List<KeyPair> peopleExtra;

	private final int maxInputs;
	private final int maxOutputs;
	private final int txNumberPerTest;
	private final double corruptedPercentage;
	private final boolean isForceInvalidPrivateKeys;
	private final boolean isForceInvalidTotals;
	private final boolean isClaimingOutputsNotInPool;
	private final boolean isForceCorruptedSignature;
	private final boolean isClaimingUtxoSeveralTimes;
	private final double maxValue;
	private final ValidationLists<Transaction> validationLists;
	private final ThreadLocalRandom random;
	private final boolean isForceNegativeOutputs;
	
	
	/**
	 * Private construct, force the creation of set with the builder
	 * @param peopleSize
	 * @param utxoTxNumber
	 * @param maxUtxoTxOutput
	 * @param maxValue
	 * @param txNumberPerTest
	 * @param maxInputs
	 * @param maxOutputs
	 * @param corruptedPercentage
	 * @param isForceInvalidPrivateKeys
	 * @param isForceInvalidTotals
	 * @param isClaimingOutputsNotInPool
	 * @param isForceCorruptedSignature
	 * @param isClaimingUtxoSeveralTimes
	 * @param isForceNegativeOutputs
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	private UtxoTestSet(int peopleSize, int utxoTxNumber, int maxUtxoTxOutput, double maxValue, int txNumberPerTest,
			int maxInputs, int maxOutputs, double corruptedPercentage, boolean isForceInvalidPrivateKeys,
			boolean isForceInvalidTotals, boolean isClaimingOutputsNotInPool, boolean isForceCorruptedSignature,
			boolean isClaimingUtxoSeveralTimes, boolean isForceNegativeOutputs)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		this.txNumberPerTest = txNumberPerTest;
		this.maxInputs = maxInputs;
		this.maxOutputs = maxOutputs;
		this.corruptedPercentage = corruptedPercentage;
		this.isForceInvalidPrivateKeys = isForceInvalidPrivateKeys;
		this.isForceInvalidTotals = isForceInvalidTotals;
		this.isClaimingOutputsNotInPool = isClaimingOutputsNotInPool;
		this.isForceCorruptedSignature = isForceCorruptedSignature;
		this.isClaimingUtxoSeveralTimes = isClaimingUtxoSeveralTimes;
		this.isForceNegativeOutputs = isForceNegativeOutputs;
		this.maxValue = maxValue;
		this.random = ThreadLocalRandom.current();

		people = createPeopleAddresses(peopleSize);
		utxoToKeyPair = new HashMap<>();
		utxoPool = createUtxoPool(people, utxoTxNumber, maxUtxoTxOutput, maxValue, utxoToKeyPair);

		peopleExtra = createPeopleAddresses(peopleSize);
		utxoExtraPool = createUtxoPool(peopleExtra, utxoTxNumber, maxUtxoTxOutput, maxValue, utxoToKeyPair);

		validationLists = generateTrxWithCorruptedSignaturePercentage();

	}

	private UTXOPool createUtxoPool(List<KeyPair> people, int utxoTxNumber, int maxUtxoTxOutput, double maxValue,
			Map<UTXO, KeyPair> utxoToKeyPair) {
		final UTXOPool utxoPool = new UTXOPool();
		Map<Integer, KeyPair> keyPairAtIndex = new HashMap<>();

		for (int i = 0; i < utxoTxNumber; i++) {
			int num = maxUtxoTxOutput;
			Transaction tx = createTxWithOutputs(people, maxValue, keyPairAtIndex, num);
			// add all tx outputs to utxo pool
			addTxOutputsToPool(utxoPool, keyPairAtIndex, utxoToKeyPair, num, tx);
		}
		return utxoPool;
	}

	private Transaction createTxWithOutputs(List<KeyPair> people, double maxValue, Map<Integer, KeyPair> keyPairAtIndex, int num) {
		final Transaction tx = new Transaction();
		for (int j = 0; j < num; j++) {
			// pick a random public address
			int rIndex = random.nextInt(people.size());
			PublicKey addr = people.get(rIndex).getPublic();
			double value = random.nextDouble(maxValue);
			tx.addOutput(value, addr);
			keyPairAtIndex.put(j, people.get(rIndex));
		}
		tx.finalize();
		return tx;
	}

	private void addTxOutputsToPool(UTXOPool utxoPool, Map<Integer, KeyPair> keyPairAtIndex,
									Map<UTXO, KeyPair> utxoToKeyPair,
									int numTx, Transaction tx) {
		for (int j = 0; j < numTx; j++) {
			UTXO ut = new UTXO(tx.getHash(), j);
			utxoPool.addUTXO(ut, tx.getOutput(j));
			utxoToKeyPair.put(ut, keyPairAtIndex.get(j));
		}
	}

	private List<KeyPair> createPeopleAddresses(int peopleSize) throws NoSuchAlgorithmException {
		final List<KeyPair> people = new ArrayList<>();
		for (int i = 0; i < peopleSize; i++)
			people.add(KeyPairGenerator.getInstance("RSA").generateKeyPair());
		return Collections.unmodifiableList(people);
	}

	private PublicKey getAddress(int rIndex) {
		return people.get(rIndex).getPublic();
	}
	
	private PrivateKey getPrivate(UTXO utxo) {
		return utxoToKeyPair.get(utxo).getPrivate();
	}
	
	private KeyPair getKeyPair(UTXO utxo) {
		return utxoToKeyPair.get(utxo);
	}

	private ArrayList<UTXO> getAllUTXO(UTXOPool pool) {
		return pool.getAllUTXO();
	}

	
	private byte[] sign(PrivateKey privateKey, byte[] rawDataToSign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initSign(privateKey);
		sig.update(rawDataToSign);
		return sig.sign();
	}


	
	private ValidationLists<Transaction> generateTrxWithCorruptedSignaturePercentage()
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		final ArrayList<UTXO> utxoList = getAllUTXO(utxoPool);
		final ArrayList<UTXO> utxoExtraList = getAllUTXO(utxoExtraPool);
		//final UTXOPool utxoPool = new UTXOPool();
		final Map<Integer, UTXO> utxoAtIndex = new HashMap<>();
		final Set<UTXO> utxosSeen = new HashSet<>();
		final Set<UTXO> utxosToRepeat = new HashSet<>();

		// create validationLists
		final List<Transaction> valid = new ArrayList<>();
		final List<Transaction> invalid = new ArrayList<>();
		final List<Transaction> conflicted = new ArrayList<>();
		for (int i = 0; i < txNumberPerTest; i++) {
			boolean corrupted = false;
			Transaction tx = new Transaction();

			final int nInputs = random.nextInt(maxInputs) + 1;
			final int nOutputs = random.nextInt(maxOutputs) + 1;

			// create inputs
			double inputValue = 0;
			for (int j = 0; j < nInputs; j++) {
				UTXO utxo = null;
				if (isClaimingOutputsNotInPool && isRandomSelection()) {
					do {
						utxo = utxoExtraList.get(random.nextInt(utxoExtraList.size()));
					} while (!utxosSeen.add(utxo));
					inputValue += utxoExtraPool.getTxOutput(utxo).value;
					corrupted = true;
				} else {
					do {
						utxo = utxoList.get(random.nextInt(utxoList.size()));
					} while (!utxosSeen.add(utxo));
					inputValue += utxoPool.getTxOutput(utxo).value;
		            if (isClaimingUtxoSeveralTimes && isRandomSelection()) {
		                utxosToRepeat.add(utxo);
		                corrupted = true;
		            }
				}
				tx.addInput(utxo.getTxHash(), utxo.getIndex());
				utxoAtIndex.put(j, utxo);
			}
			
			int count = 0;
			for (UTXO utxo : utxosToRepeat) {
				tx.addInput(utxo.getTxHash(), utxo.getIndex());
				inputValue += utxoPool.getTxOutput(utxo).value;
				utxoAtIndex.put(nInputs + count, utxo);
				count++;
			}
			
			// create outpus
			double outputValue = 0;
			for (int j = 0; j < nOutputs; j++) {
				double value;
				if ((isForceInvalidTotals && isRandomSelection())
						|| outputValue > inputValue) {
					value = random.nextDouble(maxValue);
				} else {
					value = random.nextDouble(inputValue - outputValue);
					if (isForceNegativeOutputs && isRandomSelection()) {
		                value = -value;
		                corrupted = true;
		             }
				}
	            
				tx.addOutput(value, getAddress(random.nextInt(people.size())));
				outputValue += value;
			}
			corrupted |= (outputValue > inputValue);

			// sign transaction
			for (int j = 0; j < nInputs + utxosToRepeat.size(); j++) {
				byte[] rawData = tx.getRawDataToSign(j);
				PrivateKey privateKey = getPrivate(utxoAtIndex.get(j));
				if (isRandomSelection()) {
					if (isForceInvalidPrivateKeys && isRandomSelection()) {
						// corrupt private key, change for other people
						int index = people.indexOf(getKeyPair(utxoAtIndex.get(j)));
						privateKey = people.get((index + 1) % people.size()).getPrivate();
						corrupted = true;
					} else if (isForceCorruptedSignature && isRandomSelection()) {
						// corrupt data
						rawData[0]++;
						corrupted = true;
					}
					
				}
				tx.addSignature(sign(privateKey, rawData), j);
			}
			tx.finalize();

			if (corrupted){
				invalid.add(tx);
			} else{
				valid.add(tx);
			}

		}
				
		return ValidationLists.builder(Transaction.class)
				.setValid(valid)
				.setInvalid(invalid)
				.setConflicted(conflicted)
				.build();

	}

	private boolean isRandomSelection() {
		return Math.abs(random.nextGaussian()) < corruptedPercentage;
	}
}
