const jsdom = require("jsdom");
const request = require("request-promise-native");

const { JSDOM } = jsdom;

const BASE_URL = "http://91.210.252.240";
const URL = `${BASE_URL}/broken-links`;

const processed = new Set();

let valid = 0;
let invalid = 0;

async function main() {
    await testHtmlLinks(URL + "/");

    const date = new Date().toISOString();

    console.log(`\n${valid}\n${date}`);
    console.error(`\n${invalid}\n${date}`);
}

async function testHtmlLinks(url) {
    const options = {
        resolveWithFullResponse: true
    };

    try {
        const response = await request(url, options);
        valid++;
        console.log(`${response.statusCode}\t${url}`);
        await runTest(response.body);
    } catch (error) {
        invalid++;
        console.error(`${error.statusCode}\t${url}`);
    }
}

async function runTest(content) {
    const { window } = new JSDOM(content);
    const { document } = window;
    const nodes = document.getElementsByTagName("a");
    const links = getLinksFromNodes(nodes);
    
    for (const link of links) {
        const url = `${URL}/${link}`;
        if (isLinkProcessed(url) || isLinkProcessed(link)) {
            continue;
        }
        markLinkProcessed(link);
        markLinkProcessed(url);
        if (link.startsWith("http")) {
            if (isThirdPartyUrl(link)) {
                continue;
            }
            await testHtmlLinks(link);
        } else if (url.startsWith("http")) {
            await testHtmlLinks(url);
        }
    }
}

function getLinksFromNodes(nodes) {
    return Array.from(nodes)
        .map(node => node.href.trim())
        .filter(Boolean);
}

function isLinkProcessed(link) {
    return processed.has(link);
}

function markLinkProcessed(url) {
    processed.add(url);
}

function isThirdPartyUrl(url) {
    return !url.startsWith(BASE_URL);
}

main();