package io.corexchain.verifyservice.issuer.service;

import io.corexchain.verify4j.JsonUtils;
import io.corexchain.verify4j.chainpoint.MerkleTree;
import io.corexchain.verify4j.exceptions.*;
import io.corexchain.verifyservice.issuer.model.EmployeeCardIssueDTO;
import io.corexchain.verifyservice.issuer.model.EmployeeCardRevokeDTO;
import io.nbs.contracts.CertificationRegistrationWithRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

import javax.validation.constraints.NotEmpty;
import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
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

    public String issueJson(EmployeeCardIssueDTO data) throws NoSuchAlgorithmException, SocketTimeoutException {
        Map<String, String> jsonMap = data.toMap();
        String jsonStr = JsonUtils.jsonMapToString(jsonMap);
        String hash = MerkleTree.calcHashFromStr(jsonStr, "SHA-256");
        String jsonPhoneRegnumStr = JsonUtils.jsonMapToString(data.getPhoneRegnumMap());
        String childHash = MerkleTree.calcHashFromStr(jsonPhoneRegnumStr, "SHA-256");

        CertificationRegistrationWithRole smartContract = this.getContractInstance();

        this.issueUtil(smartContract, hash, childHash, childHash);
        jsonMap.put("sc", this.contractAddress);
        return JsonUtils.jsonMapToString(jsonMap);
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
                    TransactionReceipt tr = smartContract.addCertification(hash,
                            new ArrayList<>(Collections.singleton(childHash)), certNum, BigInteger.ZERO, "v1.0-java", "").send();
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
        } catch (InvalidCreditAmountException | BlockchainNodeException | AlreadyExistsException var15) {
            throw var15;
        } catch (Exception var16) {
            logger.error(var16.getMessage(), var16);
            throw new BlockchainNodeException(var16.getMessage());
        }
    }

    public void revokeJson(EmployeeCardRevokeDTO data) throws SocketTimeoutException, NoSuchAlgorithmException {
        String jsonStr = JsonUtils.jsonMapToString(data.toMap());
        String hash = MerkleTree.calcHashFromStr(jsonStr, "SHA-256");
        CertificationRegistrationWithRole smartContract = this.getContractInstance();
        this.revokeUtil(smartContract, hash, data.revokerName);
    }

    private void revokeUtil(CertificationRegistrationWithRole smartContract, String certNum, @NotEmpty String revokerName) {
        try {
            BigInteger creditBalance = (BigInteger) smartContract.getCredit(this.issuerAddress).send();
            if (creditBalance.compareTo(BigInteger.ZERO) == 0) {
                throw new InvalidCreditAmountException("Not enough credit.");
            } else {
                // Утас болон РД-аар child hash үүсгэсэн байгаа тул олдох ёстой
                CertificationRegistrationWithRole.Certification cert = smartContract.getCertification(certNum).send();
                if (cert.id.compareTo(BigInteger.ZERO) == 0) {
                    throw new NotFoundException();
                }
                if (cert.isRevoked) {
                    return;
                }

                TransactionReceipt tr = smartContract.revoke(cert.hash, revokerName).send();
                if (!tr.isStatusOK()) {
                    throw new BlockchainNodeException("Error occurred on blockchain.");
                }

            }
        } catch (InvalidCreditAmountException | NotFoundException | AlreadyExistsException var15) {
            throw var15;
        } catch (Exception var16) {
            logger.error(var16.getMessage(), var16);
            throw new BlockchainNodeException(var16.getMessage());
        }
    }

    private CertificationRegistrationWithRole getContractInstance() throws SocketTimeoutException {
        Web3j web3j = Web3j.build(new HttpService(this.nodeUrl));
        StaticGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

        Credentials wallet = Credentials.create(issuerPk);
        RawTransactionManager transactionManager = new RawTransactionManager(web3j, wallet, this.chainId);
        CertificationRegistrationWithRole smartContract = CertificationRegistrationWithRole.load(this.contractAddress, web3j, transactionManager, gasProvider);

        try {
            smartContract.getCredit(this.issuerAddress).send();
            return smartContract;
        } catch (SocketTimeoutException var6) {
            throw var6;
        } catch (InterruptedIOException var7) {
            throw new SocketTimeoutException(var7.getMessage());
        } catch (Exception var8) {
            throw new InvalidSmartContractException();
        }
    }
}
