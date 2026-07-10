package com.legalknowledge.common.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legalknowledge.mapper.LegalDocumentMapper;
import com.legalknowledge.mapper.UserMapper;
import com.legalknowledge.entity.LegalDocument;
import com.legalknowledge.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserMapper userMapper;
    private final LegalDocumentMapper documentMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserMapper userMapper,
                            LegalDocumentMapper documentMapper,
                            PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.documentMapper = documentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initAdmin();
        initSeedDocuments();
    }

    private void initAdmin() {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, "admin"));
        if (count > 0) return;

        User admin = new User(
                "admin",
                "admin@legalknowledge.com",
                passwordEncoder.encode("admin123"),
                User.ROLE_ADMIN
        );
        userMapper.insert(admin);
        log.info("默认管理员已创建: admin / admin123");
    }

    private void initSeedDocuments() {
        Long count = documentMapper.selectCount(null);
        if (count > 0) {
            log.info("法规数据已存在，跳过种子数据初始化");
            return;
        }

        log.info("开始初始化劳动法相关法规数据...");

        List<LegalDocument> docs = List.of(
                createDoc("中华人民共和国劳动法", "主席令第28号",
                        "全国人民代表大会常务委员会",
                        LocalDate.of(1994, 7, 5), LocalDate.of(1995, 1, 1),
                        "劳动法", "法律", "现行有效",
                        "第一章 总则\n第一条 为了保护劳动者的合法权益，调整劳动关系，建立和维护适应社会主义市场经济的劳动制度，促进经济发展和社会进步，根据宪法，制定本法。\n第二条 在中华人民共和国境内的企业、个体经济组织（以下统称用人单位）和与之形成劳动关系的劳动者，适用本法。国家机关、事业组织、社会团体和与之建立劳动合同关系的劳动者，依照本法执行。\n\n第二章 促进就业\n第十条 国家通过促进经济和社会发展，创造就业条件，扩大就业机会。\n第十二条 劳动者就业，不因民族、种族、性别、宗教信仰不同而受歧视。\n\n第三章 劳动合同和集体合同\n第十六条 劳动合同是劳动者与用人单位确立劳动关系、明确双方权利和义务的协议。建立劳动关系应当订立劳动合同。\n第十九条 劳动合同应当以书面形式订立，并具备以下条款：（一）劳动合同期限；（二）工作内容；（三）劳动保护和劳动条件；（四）劳动报酬；（五）劳动纪律；（六）劳动合同终止的条件；（七）违反劳动合同的责任。\n第二十一条 劳动合同可以约定试用期。试用期最长不得超过六个月。\n\n第四章 工作时间和休息休假\n第三十六条 国家实行劳动者每日工作时间不超过八小时、平均每周工作时间不超过四十四小时的工时制度。\n第三十八条 用人单位应当保证劳动者每周至少休息一日。\n第四十四条 有下列情形之一的，用人单位应当按照下列标准支付高于劳动者正常工作时间工资的工资报酬：（一）安排劳动者延长工作时间的，支付不低于工资的百分之一百五十的工资报酬；（二）休息日安排劳动者工作又不能安排补休的，支付不低于工资的百分之二百的工资报酬；（三）法定休假日安排劳动者工作的，支付不低于工资的百分之三百的工资报酬。\n\n第五章 工资\n第四十六条 工资分配应当遵循按劳分配原则，实行同工同酬。\n第四十八条 国家实行最低工资保障制度。\n第五十条 工资应当以货币形式按月支付给劳动者本人。不得克扣或者无故拖欠劳动者的工资。\n\n第七章 女职工和未成年工特殊保护\n第五十八条 国家对女职工和未成年工实行特殊劳动保护。\n第六十二条 女职工生育享受不少于九十天的产假。\n\n第十章 劳动争议\n第七十七条 用人单位与劳动者发生劳动争议，当事人可以依法申请调解、仲裁、提起诉讼，也可以协商解决。\n第七十九条 劳动争议发生后，当事人可以向本单位劳动争议调解委员会申请调解；调解不成，当事人一方要求仲裁的，可以向劳动争议仲裁委员会申请仲裁。对仲裁裁决不服的，可以向人民法院提起诉讼。"),

                createDoc("中华人民共和国劳动合同法", "主席令第65号",
                        "全国人民代表大会常务委员会",
                        LocalDate.of(2007, 6, 29), LocalDate.of(2008, 1, 1),
                        "劳动法", "法律", "现行有效",
                        "第一章 总则\n第一条 为了完善劳动合同制度，明确劳动合同双方当事人的权利和义务，保护劳动者的合法权益，构建和发展和谐稳定的劳动关系，制定本法。\n第三条 订立劳动合同，应当遵循合法、公平、平等自愿、协商一致、诚实信用的原则。\n\n第二章 劳动合同的订立\n第十条 建立劳动关系，应当订立书面劳动合同。已建立劳动关系，未同时订立书面劳动合同的，应当自用工之日起一个月内订立书面劳动合同。\n第十四条 有下列情形之一，劳动者提出或者同意续订、订立劳动合同的，除劳动者提出订立固定期限劳动合同外，应当订立无固定期限劳动合同：（一）劳动者在该用人单位连续工作满十年的；（二）连续订立二次固定期限劳动合同，且续订劳动合同的。\n第十九条 劳动合同期限三个月以上不满一年的，试用期不得超过一个月；一年以上不满三年的，试用期不得超过二个月；三年以上的，试用期不得超过六个月。同一用人单位与同一劳动者只能约定一次试用期。\n\n第四章 劳动合同的解除和终止\n第三十七条 劳动者提前三十日以书面形式通知用人单位，可以解除劳动合同。劳动者在试用期内提前三日通知用人单位，可以解除劳动合同。\n第三十八条 用人单位未及时足额支付劳动报酬的、未依法为劳动者缴纳社会保险费的，劳动者可以解除劳动合同。\n第四十六条 有下列情形之一的，用人单位应当向劳动者支付经济补偿。\n第四十七条 经济补偿按劳动者在本单位工作的年限，每满一年支付一个月工资的标准向劳动者支付。"),

                createDoc("中华人民共和国劳动争议调解仲裁法", "主席令第80号",
                        "全国人民代表大会常务委员会",
                        LocalDate.of(2007, 12, 29), LocalDate.of(2008, 5, 1),
                        "劳动法", "法律", "现行有效",
                        "第一章 总则\n第一条 为了公正及时解决劳动争议，保护当事人合法权益，促进劳动关系和谐稳定，制定本法。\n第二条 中华人民共和国境内的用人单位与劳动者发生的下列劳动争议，适用本法：（一）因确认劳动关系发生的争议；（二）因订立、履行、变更、解除和终止劳动合同发生的争议；（三）因除名、辞退和辞职、离职发生的争议；（四）因工作时间、休息休假、社会保险、福利、培训以及劳动保护发生的争议；（五）因劳动报酬、工伤医疗费、经济补偿或者赔偿金等发生的争议。\n\n第三章 仲裁\n第二十一条 劳动争议由劳动合同履行地或者用人单位所在地的劳动争议仲裁委员会管辖。\n第二十七条 劳动争议申请仲裁的时效期间为一年。劳动关系存续期间因拖欠劳动报酬发生争议的，劳动者申请仲裁不受一年仲裁时效期间的限制；但是劳动关系终止的，应当自劳动关系终止之日起一年内提出。\n第四十三条 仲裁庭裁决劳动争议案件，应当自劳动争议仲裁委员会受理仲裁申请之日起四十五日内结束。"),

                createDoc("工伤保险条例", "国务院令第586号",
                        "国务院",
                        LocalDate.of(2010, 12, 20), LocalDate.of(2011, 1, 1),
                        "劳动法", "行政法规", "现行有效",
                        "第一章 总则\n第一条 为了保障因工作遭受事故伤害或者患职业病的职工获得医疗救治和经济补偿，促进工伤预防和职业康复，分散用人单位的工伤风险，制定本条例。\n\n第三章 工伤认定\n第十四条 职工有下列情形之一的，应当认定为工伤：（一）在工作时间和工作场所内，因工作原因受到事故伤害的；（二）工作时间前后在工作场所内，从事与工作有关的预备性或者收尾性工作受到事故伤害的；（三）在工作时间和工作场所内，因履行工作职责受到暴力等意外伤害的；（四）患职业病的；（五）因工外出期间，由于工作原因受到伤害或者发生事故下落不明的；（六）在上下班途中，受到非本人主要责任的交通事故或者城市轨道交通、客运轮渡、火车事故伤害的。\n第十七条 职工发生事故伤害或者被诊断、鉴定为职业病，所在单位应当自事故伤害发生之日或者被诊断、鉴定为职业病之日起30日内，向社会保险行政部门提出工伤认定申请。\n\n第五章 工伤保险待遇\n第三十七条 职工因工死亡，其近亲属领取丧葬补助金、供养亲属抚恤金和一次性工亡补助金。一次性工亡补助金标准为上一年度全国城镇居民人均可支配收入的20倍。"),

                createDoc("职工带薪年休假条例", "国务院令第514号",
                        "国务院",
                        LocalDate.of(2007, 12, 14), LocalDate.of(2008, 1, 1),
                        "劳动法", "行政法规", "现行有效",
                        "第一条 为了维护职工休息休假权利，调动职工工作积极性，根据劳动法和公务员法，制定本条例。\n第二条 机关、团体、企业、事业单位、民办非企业单位、有雇工的个体工商户等单位的职工连续工作1年以上的，享受带薪年休假。职工在年休假期间享受与正常工作期间相同的工资收入。\n第三条 职工累计工作已满1年不满10年的，年休假5天；已满10年不满20年的，年休假10天；已满20年的，年休假15天。\n第五条 单位确因工作需要不能安排职工休年休假的，经职工本人同意，可以不安排。对职工应休未休的年休假天数，单位应当按照该职工日工资收入的300%支付年休假工资报酬。")
        );

        for (LegalDocument doc : docs) {
            documentMapper.insert(doc);
        }

        log.info("种子数据初始化完成，共 {} 条", documentMapper.selectCount(null));
    }

    private LegalDocument createDoc(String title, String number, String authority,
                                     LocalDate publishDate, LocalDate effectiveDate,
                                     String category, String level, String status, String content) {
        LegalDocument doc = new LegalDocument();
        doc.setTitle(title);
        doc.setDocumentNumber(number);
        doc.setIssuingAuthority(authority);
        doc.setPublishDate(publishDate);
        doc.setEffectiveDate(effectiveDate);
        doc.setCategory(category);
        doc.setLevel(level);
        doc.setStatus(status);
        doc.setContent(content);
        return doc;
    }
}
