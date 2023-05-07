import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as events from 'aws-cdk-lib/aws-events'
import * as targets from 'aws-cdk-lib/aws-events-targets'

export class KTGBotAPIRSSOverflowStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const entriesTable = new dynamodb.Table(this, 'ktgbotapi-rssoverflow-table-entries', {
            tableName: 'ktgbotapi-rssoverflow-table-entries',
            partitionKey: {name: 'id', type: dynamodb.AttributeType.STRING},
            billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
            removalPolicy: cdk.RemovalPolicy.DESTROY,
        });

        const checkerLambda = new lambda.Function(this, 'ktgbotapi-rssoverflow-lambda-checker', {
            functionName: 'ktgbotapi-rssoverflow-lambda-checker',
            runtime: lambda.Runtime.JAVA_11,
            timeout: cdk.Duration.seconds(10),
            maxEventAge: cdk.Duration.minutes(5),
            retryAttempts: 0,
            memorySize: 768,
            code: lambda.Code.fromAsset('../build/libs/ktgbotapi-rssoverflow-all.jar'),
            handler: 'me.madhead.ktgbotapi.so.rss.Handler',
            environment: {
                'LOG_THRESHOLD': 'INFO',
                'TABLE_ENTRIES': entriesTable.tableName,
                'TOKEN_TELEGRAM': this.node.tryGetContext('TOKEN_TELEGRAM'),
                'CHAT_ID': this.node.tryGetContext('CHAT_ID'),
                'THREAD_ID': this.node.tryGetContext('THREAD_ID'),
            },
        });

        entriesTable.grantReadWriteData(checkerLambda);

        const checkRule = new events.Rule(this, 'ktgbotapi-rssoverflow-rule-check', {
            ruleName: 'ktgbotapi-rssoverflow-rule-check',
            schedule: events.Schedule.rate(cdk.Duration.minutes(10)),
        });

        checkRule.addTarget(new targets.LambdaFunction(checkerLambda));
    }
}
