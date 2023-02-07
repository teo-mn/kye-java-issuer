package io.corexchain.verifyservice.issuer.service;

import io.corexchain.verify4j.JsonUtils;
import io.corexchain.verify4j.chainpoint.MerkleTree;
import io.corexchain.verify4j.exceptions.*;
import io.corexchain.verifyservice.issuer.model.*;
import io.nbs.contracts.CertificationRegistrationWithRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

import javax.validation.constraints.NotEmpty;
import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmployeeCardIssuerService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeCardIssuerService.class);

    protected static BigInteger GAS_PRICE = BigInteger.valueOf(1000000000000L);
    protected static BigInteger GAS_LIMIT = BigInteger.valueOf(2000000L);
    @Value("${verify.service.blockchain.node.url}")
    private String nodeUrl;
    @Value("${verify.service.blockchain.contract.address}")
    private String contractAddress;
    @Value("${verify.service.blockchain.issuer.address}")
    private String issuerAddress;
    @Value("${verify.service.blockchain.issuer.pk}")
    private String issuerPk;
    @Value("${verify.service.blockchain.node.chainId}")
    private Integer chainId;

    public String issueJson(EmployeeCardIssueRequestDTO request) throws NoSuchAlgorithmException, SocketTimeoutException {
        EmployeeCardIssueDTO data = request.getData();
        Map<String, String> jsonMap = data.toMap();
        String jsonStr = JsonUtils.jsonMapToString(jsonMap);
        String hash = MerkleTree.calcHashFromStr(jsonStr, "SHA-256");
        String jsonPhoneRegnumStr = JsonUtils.jsonMapToString(data.getPhoneRegnumMap());
        String childHash = MerkleTree.calcHashFromStr(jsonPhoneRegnumStr, "SHA-256");
        String certNumJsonStr = JsonUtils.jsonMapToString(data.getCertNumMap());
        String certNumHash = MerkleTree.calcHashFromStr(certNumJsonStr, "SHA-256");

        CertificationRegistrationWithRole smartContract = this.getContractInstance(this.contractAddress);

        this.issueUtil(smartContract, hash, childHash, certNumHash);
        jsonMap.put("sc", this.contractAddress);
        String res = "";
        res += "{";
        res += "\"requestId\": " + "\"" + request.getRequestId() + "\", ";
        res += "\"action\": " + "\"" + request.getAction() + "\", ";
        res += "\"data\": "  + JsonUtils.jsonMapToString(jsonMap);
        res += "}";
        return res;
    }

    private String issueUtil(CertificationRegistrationWithRole smartContract, String hash, String childHash, String certNum) {
        try {
            BigInteger creditBalance = smartContract.getCredit(this.issuerAddress).send();
            if (creditBalance.compareTo(BigInteger.ZERO) == 0) {
                throw new InvalidCreditAmountException("Not enough credit.");
            } else {
                CertificationRegistrationWithRole.Certification cert = smartContract.getCertification(hash).send();
                if (cert.id.compareTo(BigInteger.ZERO) != 0 && !cert.isRevoked) {
                    throw new AlreadyExistsException("Certification hash already existed in the smart contract.");
                } else {

                    // check child hash
                    CertificationRegistrationWithRole.Certification cert2 = smartContract.getCertificationByCertNum(certNum).send();
                    if (!cert2.isRevoked && cert2.id.compareTo(BigInteger.ZERO) != 0) {
                        throw new AlreadyExistsException("Certification hash already existed in the smart contract.");
                    }
                    cert2 = smartContract.getCertification(childHash).send();
                    TransactionReceipt tr;
                    if (!cert2.isRevoked && cert2.id.compareTo(BigInteger.ZERO) != 0) {
                        tr = smartContract.addCertification(hash,
                                certNum, BigInteger.ZERO, "v1.0-java", "").send();
                    } else {
                        tr = smartContract.addCertification(hash,
                                new ArrayList<>(Collections.singleton(childHash)), certNum, BigInteger.ZERO, "v1.0-java", "").send();
                    }

                    if (!tr.isStatusOK()) {
                        throw new BlockchainNodeException("Error occurred on blockchain.");
                    } else {
                        try {
                            smartContract.addTransactionId(hash, tr.getTransactionHash()).send();
                        } catch (Exception ignored) {
                        }

                        return tr.getTransactionHash();
                    }
                }
            }
        } catch (InvalidCreditAmountException | BlockchainNodeException | AlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BlockchainNodeException(e.getMessage());
        }
    }

    public String revokeJson(EmployeeCardRevokeRequestDTO request) throws SocketTimeoutException, NoSuchAlgorithmException {
        EmployeeCardRevokeDTO data = request.getData();
        String jsonStr = JsonUtils.jsonMapToString(data.getCertNumMap());
        String hash = MerkleTree.calcHashFromStr(jsonStr, "SHA-256");
        CertificationRegistrationWithRole smartContract = this.getContractInstance(this.contractAddress);
        this.revokeUtil(smartContract, hash, data.getRevokerName());

        String res = "";
        res += "{";
        res += "\"requestId\": " + "\"" + request.getRequestId() + "\", ";
        res += "\"action\": " + "\"" + request.getAction() + "\", ";
        res += "\"data\": "  + jsonStr;
        res += "}";
        return res;
    }

    private void revokeUtil(CertificationRegistrationWithRole smartContract, String certNum, @NotEmpty String revokerName) {
        try {
            BigInteger creditBalance = (BigInteger) smartContract.getCredit(this.issuerAddress).send();
            if (creditBalance.compareTo(BigInteger.ZERO) == 0) {
                throw new InvalidCreditAmountException("Not enough credit.");
            } else {
                // Утас болон РД-аар child hash үүсгэсэн байгаа тул олдох ёстой
                CertificationRegistrationWithRole.Certification cert = smartContract.getCertificationByCertNum(certNum).send();
                if (cert.id.compareTo(BigInteger.ZERO) == 0) {
                    throw new NotFoundException("The HASH value is not found in blockchain.");
                }
                if (cert.isRevoked) {
                    return;
                }

                TransactionReceipt tr = smartContract.revoke(cert.hash, revokerName).send();
                if (!tr.isStatusOK()) {
                    throw new BlockchainNodeException("Error occurred on blockchain.");
                }

            }
        } catch (InvalidCreditAmountException | NotFoundException | AlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BlockchainNodeException(e.getMessage());
        }
    }

    public boolean isValid(EmployeeCardDTO card, String smartContractAddress) throws SocketTimeoutException, NoSuchAlgorithmException {
        if (!StringUtils.hasLength(smartContractAddress))
            smartContractAddress = this.contractAddress;
        CertificationRegistrationWithRole smartContract = this.getContractReadOnlyInstance(smartContractAddress);
        String jsonStr = JsonUtils.jsonMapToString(card.getPhoneRegnumMap());
        String hash = MerkleTree.calcHashFromStr(jsonStr, "SHA-256");
        CertificationRegistrationWithRole.Certification cert;
        try {
            // Утас болон РД-аар child hash үүсгэсэн байгаа тул олдох ёстой
            cert = smartContract.getCertification(hash).send();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BlockchainNodeException(e.getMessage());
        }
        if (cert.id.compareTo(BigInteger.ZERO) == 0)
            throw new NotFoundException("The HASH value is not found in blockchain.");

        return !cert.isRevoked;
    }

    private CertificationRegistrationWithRole getContractInstance(String smartContractAddress) throws SocketTimeoutException {
        Web3j web3j = Web3j.build(new HttpService(this.nodeUrl));
        StaticGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

        Credentials wallet = Credentials.create(issuerPk);
        RawTransactionManager transactionManager = new RawTransactionManager(web3j, wallet, this.chainId, 100, 200);
        CertificationRegistrationWithRole smartContract = CertificationRegistrationWithRole.load(smartContractAddress, web3j, transactionManager, gasProvider);

        try {
            smartContract.getCredit(this.issuerAddress).send();
            return smartContract;
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (InterruptedIOException e) {
            throw new SocketTimeoutException(e.getMessage());
        } catch (Exception e) {
            throw new InvalidSmartContractException();
        }
    }

    private CertificationRegistrationWithRole getContractReadOnlyInstance(String smartContractAddress) {
        Web3j web3j = Web3j.build(new HttpService(this.nodeUrl));
        StaticGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
        ReadonlyTransactionManager transactionManager = new ReadonlyTransactionManager(web3j, smartContractAddress);
        CertificationRegistrationWithRole smartContract = CertificationRegistrationWithRole.load(smartContractAddress, web3j, transactionManager, gasProvider);
        return smartContract;
    }
}
