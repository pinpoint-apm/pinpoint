


// public class AlarmMessageSenderImple implements AlarmMessageSender {

//     private final Logger logger = LoggerFactory.getLogger(this.getClass());

//     @Autowired
//     private UserGroupService userGroupService;
    
//     @Override
//     public void sendSms(AlarmChecker checker, int sequenceCount) {
//         List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getUserGroupId());

//         if (receivers.size() == 0) {
//             return;
//         }

//         for (String message : checker.getSmsMessage()) {
//             logger.info("send SMS : {}", message);

//             // TODO Implement logic for sending SMS
//         }
//     }

//     @Override
//     public void sendEmail(AlarmChecker checker, int sequenceCount) {
//         List<String> receivers = userGroupService.selectEmailOfMember(checker.getUserGroupId());

//         if (receivers.size() == 0) {
//             return;
//         }

//         for (String message : checker.getEmailMessage()) {
//             logger.info("send email : {}", message);

//             // TODO Implement logic for sending email
//         }
//     }
// }