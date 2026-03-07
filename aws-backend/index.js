import { BedrockRuntimeClient, ConverseCommand } from "@aws-sdk/client-bedrock-runtime";

const client = new BedrockRuntimeClient({ region: "us-east-1" });
const MODEL_ID = process.env.BEDROCK_MODEL_ID || "us.amazon.nova-2-lite-v1:0";

function extractJsonFromText(text) {
    try {
        return JSON.parse(text);
    } catch (e) {
        const match = text.match(/\{[\s\S]*\}/);
        if (match) {
            return JSON.parse(match[0]);
        }
        throw e;
    }
}

async function invokeNova({ messages, system }) {
    const command = new ConverseCommand({
        modelId: MODEL_ID,
        messages,
        system
    });

    return client.send(command);
}

function extractOutputText(response) {
    return response?.output?.message?.content
        ?.filter((contentPart) => typeof contentPart?.text === "string")
        .map((contentPart) => contentPart.text)
        .join("\n")
        .trim() || "";
}

function logBedrockError(context, error) {
    console.error(`${context}:`, {
        modelId: MODEL_ID,
        name: error?.name,
        message: error?.message,
        code: error?.code,
        statusCode: error?.$metadata?.httpStatusCode,
        requestId: error?.$metadata?.requestId,
        cfId: error?.$metadata?.cfId,
        extendedRequestId: error?.$metadata?.extendedRequestId,
        stack: error?.stack
    });
}

export const handler = async (event) => {
    console.log("Received event:", JSON.stringify(event, null, 2));

    try {
        // Handle CORS preflight requests
        if (event.httpMethod === "OPTIONS") {
            return buildResponse(200, "OK");
        }

        const path = event.rawPath || event.requestContext?.http?.path || event.resource || event.path || '';
        const body = event.body ? (typeof event.body === 'string' ? JSON.parse(event.body) : event.body) : {};

        // Strip out the API gateway stage name (like '/default') if it exists
        const cleanPath = path.replace('/default', '');

        switch (cleanPath) {
            case '/analyze-item':
                return await handleAnalyzeItem(body);
            case '/analyze-user':
                return await handleAnalyzeUser(body);
            case '/generate-outfit':
                return await handleGenerateOutfit(body);
            case '/chat':
                return await handleChat(body);
            default:
                return buildResponse(404, { error: `Route not found: ${cleanPath}` });
        }
    } catch (error) {
        console.error("Error processing request:", error);
        return buildResponse(500, { error: "Internal server error" });
    }
};

/**
 * Handles the Moderation and Categorization of uploaded images.
 * Expects a JSON body with a base64 encoded image string.
 */
async function handleAnalyzeItem(body) {
    if (!body.imageBase64) {
        return buildResponse(400, { error: "Missing imageBase64 in request body" });
    }

    const systemPrompt = `You are a helpful AI assistant for a digital wardrobe application.
Your job is to analyze images uploaded by users and extract their clothing category, color, and style.
First, determine if this image contains explicit content, adult content, nudity, or identifiable human faces. If it does, return ONLY the exact text: REJECTED_CONTENT.
If it is a safe image, next determine if it is actually a piece of clothing, footwear, or a wearable accessory.
CRITICAL: If the image is NOT clearly a piece of clothing or footwear (e.g., a photo of a chair, dog, laptop, plain room), do NOT guess. Return ONLY the exact text: REJECTED_NOT_CLOTHING.
For safe clothing items, output a JSON object containing FOUR fields: 
1. "name": A short, descriptive name for the item (e.g., "Navy Blue Chinos", "Red Checkered Shirt"). Max 4 words.
2. "category": You MUST choose EXCLUSIVELY from one of these exact strings: "Top", "Bottom", "Shoes", or "Accessory". Do NOT use any other category names. Be very strict (e.g., only classify actual shoes/sneakers/boots as "Shoes", do not classify a random object as shoes).
3. "color" (e.g., Red, Blue, Black).
4. "style" (e.g., Casual, Formal, Sporty, Winter, Summer). 
Output ONLY valid JSON, nothing else.`;

    const payload = {
        messages: [
            {
                role: "user",
                content: [
                    {
                        image: {
                            format: "jpeg", // assuming jpeg from the Android app
                            source: {
                                bytes: body.imageBase64
                            }
                        }
                    },
                    {
                        text: "Please analyze this image according to your instructions."
                    }
                ]
            }
        ],
        system: [{ text: systemPrompt }]
    };

    try {
        const result = await invokeNova(payload);
        const outputText = extractOutputText(result);

        if (outputText === "REJECTED_CONTENT") {
            return buildResponse(400, { success: false, error: "Image rejected due to explicit content or faces." });
        }
        if (outputText === "REJECTED_NOT_CLOTHING") {
            return buildResponse(400, { success: false, error: "Image does not appear to be a clothing item." });
        }

        try {
            const parsedData = extractJsonFromText(outputText);
            return buildResponse(200, {
                success: true,
                name: parsedData.name || "Unknown Item",
                category: parsedData.category,
                color: parsedData.color,
                style: parsedData.style || "Casual"
            });
        } catch (jsonError) {
            console.error("Failed to parse Nova JSON output:", outputText);
            return buildResponse(500, { success: false, error: "Failed to parse AI output." });
        }

    } catch (error) {
        logBedrockError("Bedrock invocation error during analyze-item", error);
        return buildResponse(500, { success: false, error: "Failed to interact with Amazon Nova." });
    }
}

/**
 * Handles analyzing a user's selfie to determine skin tone and color palette.
 * Expects a JSON body with a base64 encoded image string.
 */
async function handleAnalyzeUser(body) {
    if (!body.imageBase64) {
        return buildResponse(400, { error: "Missing imageBase64 in request body" });
    }

    const systemPrompt = `You are a specialized AI color analyst for a digital wardrobe application.
Your job is to analyze a selfie uploaded by a user and extract their skin tone and seasonal color palette.
First, determine if this image contains explicit content, adult content, or nudity. If it does, return ONLY the exact text: REJECTED_CONTENT.
Note: For this specific request, YOU MUST ALLOW identifiable human faces. Do NOT reject the image for having a face. You are explicitly authorized to analyze the user's skin tone. Do not identify who the person is.
If the image is safe, output a JSON object containing two fields: "skinTone" (e.g., Fair, Light, Medium, Deep, Dark) and "palette" (e.g., Winter, Spring, Summer, Autumn). Output ONLY valid JSON, nothing else.`;

    const payload = {
        messages: [
            {
                role: "user",
                content: [
                    {
                        image: {
                            format: "jpeg",
                            source: { bytes: body.imageBase64 }
                        }
                    },
                    {
                        text: "Please analyze my skin tone and seasonal color palette."
                    }
                ]
            }
        ],
        system: [{ text: systemPrompt }]
    };

    try {
        const result = await invokeNova(payload);
        const outputText = extractOutputText(result);

        if (outputText === "REJECTED_CONTENT") {
            return buildResponse(400, { success: false, error: "Image rejected due to explicit content." });
        }

        try {
            const parsedData = extractJsonFromText(outputText);
            return buildResponse(200, {
                success: true,
                skinTone: parsedData.skinTone,
                palette: parsedData.palette
            });
        } catch (jsonError) {
            console.error("Failed to parse Nova JSON output:", outputText);
            return buildResponse(500, { success: false, error: "Failed to parse AI output." });
        }
    } catch (error) {
        logBedrockError("Bedrock invocation error during analyze-user", error);
        return buildResponse(500, { success: false, error: "Failed to interact with Amazon Nova." });
    }
}

/**
 * Generates an outfit using Amazon Nova based on weather and available wardrobe items.
 * Expects a JSON body with weather data and available items.
 */
async function handleGenerateOutfit(body) {
    const { latitude, longitude, currentWardrobeItems, userProfile } = body;

    if (!currentWardrobeItems || currentWardrobeItems.length < 3) {
        return buildResponse(400, { success: false, error: "Need at least 3 items to generate an outfit." });
    }

    // Step 1: Fetch Weather and Location Name
    let weatherCondition = "Clear";
    let temperatureCelsius = 22;
    let locationName = "Unknown Location";
    try {
        const weatherRes = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${latitude}&longitude=${longitude}&current_weather=true`);
        if (weatherRes.ok) {
            const weatherData = await weatherRes.json();
            temperatureCelsius = weatherData.current_weather.temperature;
            // Map simple WMO codes or just use generic text
            const code = weatherData.current_weather.weathercode;
            if (code >= 50 && code <= 69) weatherCondition = "Rainy";
            else if (code >= 71 && code <= 79) weatherCondition = "Snowy";
            else if (code > 0 && code <= 3) weatherCondition = "Cloudy";
        }

        // Fetch location name using Open-Meteo Geocoding API (using reverse geocoding approach or nearest city if possible)
        // Note: Open-Meteo doesn't have a direct reverse-geocoding API, so we'll just format the lat/lon if needed, 
        // or we can use another free geocoding API like Nominatim if required. 
        // For simplicity, let's just return a placeholder or use the coordinates for now if no API is available.
        // Actually, let's try calling BigDataCloud's free reverse geocoding API which requires no key:
        const geoRes = await fetch(`https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=en`);
        if (geoRes.ok) {
            const geoData = await geoRes.json();
            locationName = geoData.city || geoData.locality || "Unknown Location";
        }
    } catch (e) {
        console.warn("Weather/Geocoding fetch failed, using default.", e);
    }

    // Step 2: Prompt Amazon Nova
    const systemPrompt = `You are an expert AI fashion assistant. 
You will be provided with current weather data, a JSON list of available wardrobe items (each with an id, category, color, and style), and optionally the user's skin tone and seasonal color palette.
Your task is to select EXACTLY ONE top (Category: Top), EXACTLY ONE bottom (Category: Bottom), and EXACTLY ONE pair of shoes (Category: Shoes) from the provided list that make a stylish, weather-appropriate outfit that flatters the user's specific skin tone and palette if provided.
Return ONLY a valid JSON object with these exact keys: "topId", "bottomId", "shoesId", "aiReasoning".
Do not include any other text, markdown formatting, or explanation outside the JSON.
CRITICAL INSTRUCTION: Never include the raw item 'id' (UUID) in the "aiReasoning" string. Focus your reasoning on the colors, styles, weather, and skin tone.`;

    let userProfileText = "";
    if (userProfile && userProfile.skinTone) {
        userProfileText = `User Profile: Skin Tone is ${userProfile.skinTone}, Color Palette is ${userProfile.palette}.`;
    }

    const payload = {
        messages: [
            {
                role: "user",
                content: [
                    {
                        text: `Weather: ${temperatureCelsius}°C, ${weatherCondition}.
${userProfileText}
Wardrobe items: ${JSON.stringify(currentWardrobeItems)}
Please pick the best outfit.`
                    }
                ]
            }
        ],
        system: [{ text: systemPrompt }]
    };

    try {
        const result = await invokeNova(payload);
        let outputText = extractOutputText(result);

        let parsedOutfit;
        try {
            parsedOutfit = extractJsonFromText(outputText);
        } catch (e) {
            console.error("Failed to parse Nova JSON", outputText);
            return buildResponse(500, { success: false, error: "Failed to parse AI output." });
        }

        return buildResponse(200, {
            success: true,
            topId: parsedOutfit.topId,
            bottomId: parsedOutfit.bottomId,
            shoesId: parsedOutfit.shoesId,
            weatherCondition: weatherCondition,
            temperatureCelsius: temperatureCelsius,
            locationName: locationName,
            aiReasoning: parsedOutfit.aiReasoning || "I selected this based on the temperature."
        });

    } catch (error) {
        logBedrockError("Bedrock invocation error during generate-outfit", error);
        return buildResponse(500, { success: false, error: "Failed to interact with Amazon Nova." });
    }
}

/**
 * Handles conversational chat requests.
 * Expects a JSON body with "message", "wardrobe" (array of items), and optionally "userProfile".
 */
async function handleChat(body) {
    if (!body.message) {
        return buildResponse(400, { error: "Missing message in request body" });
    }

    const { message, wardrobe, userProfile } = body;

    let userProfileText = "";
    if (userProfile && userProfile.skinTone) {
        userProfileText = `The user has a "${userProfile.skinTone}" skin tone and a "${userProfile.palette}" seasonal color palette.\n`;
    }

    const systemPrompt = `You are a helpful and stylish AI fashion planner. 
The user is asking for fashion advice, event styling, or vacation packing lists.
You have access to their current wardrobe inventory below (provided as JSON).
${userProfileText}

When answering, reference the user's specific items by category, color, and style whenever possible.
CRITICAL INSTRUCTION: You must return ONLY a JSON object with two keys:
1. "reply": Your conversational response formatting clearly with markdown bullet points if answering a packing list. Never reveal raw UUIDs in this text.
2. "outfits": An array of outfit objects that correlate to your advice. Each object MUST contain EXACTLY one "topId", one "bottomId", and one "shoesId" mapped from the user's wardrobe inventory. If the user doesn't have a matching item to complete the trio, omit that outfit entirely. The array can be empty if you are just chatting and not suggesting outfits.

Output ONLY valid JSON, nothing else.`;

    const wardrobeContext = wardrobe && wardrobe.length > 0
        ? `Here is my current wardrobe JSON:\n${JSON.stringify(wardrobe, null, 2)}\n\n`
        : "I haven't added any items to my wardrobe yet.\n\n";

    const promptText = `${wardrobeContext}My question: ${message}`;

    const payload = {
        messages: [{ role: "user", content: [{ text: promptText }] }],
        system: [{ text: systemPrompt }]
    };

    try {
        const result = await invokeNova(payload);
        const outputText = extractOutputText(result);

        try {
            const parsedData = extractJsonFromText(outputText);
            return buildResponse(200, {
                success: true,
                reply: parsedData.reply || "I couldn't generate a proper response.",
                outfits: parsedData.outfits || []
            });
        } catch (jsonError) {
            console.error("Failed to parse Nova JSON chat output:", outputText);
            // Fallback strategy if AI acts weird
            return buildResponse(200, {
                success: true,
                reply: outputText,
                outfits: []
            });
        }
    } catch (e) {
        logBedrockError("Chat invocation failed", e);
        return buildResponse(500, { success: false, error: "Failed to generate chat response." });
    }
}

function buildResponse(statusCode, body) {
    return {
        statusCode: statusCode,
        headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Headers": "Content-Type",
            "Access-Control-Allow-Methods": "OPTIONS,POST,GET"
        },
        body: JSON.stringify(body)
    };
}
