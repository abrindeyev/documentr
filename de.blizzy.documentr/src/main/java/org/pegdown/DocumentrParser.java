/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.pegdown;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.Rule;
import org.parboiled.annotations.Cached;
import org.parboiled.common.ArrayBuilder;
import org.parboiled.matchers.AnyOfMatcher;
import org.parboiled.support.StringBuilderVar;
import org.parboiled.support.StringVar;

import de.blizzy.documentr.web.markdown.MacroNode;

public class DocumentrParser extends Parser {
	private static final int PEGDOWN_OPTIONS = Extensions.ALL -
			Extensions.QUOTES - Extensions.SMARTS - Extensions.SMARTYPANTS;

	public DocumentrParser() {
		super(Integer.valueOf(PEGDOWN_OPTIONS));
	}

	@Override
	public Rule NonLinkInline() {
		return FirstOf(new ArrayBuilder<Rule>()
				.add(
						StandaloneMacro(),
						Str(), Endline(), UlOrStarLine(), Space(), StrongOrEmph(), Image(), Code(), InlineHtml(),
						Entity(), EscapedChar())
				.addNonNulls(ext(QUOTES) ? new Rule[] { SingleQuoted(), DoubleQuoted(), DoubleAngleQuoted() } : null)
				.addNonNulls(ext(SMARTS) ? new Rule[] { Smarts() } : null)
				.add(Symbol()).get());
	}

	public Rule StandaloneMacro() {
		StringVar macroName = new StringVar();
		StringVar params = new StringVar();
		return Sequence(
				Test(StandaloneMacroOpen(macroName, params)),
				NamedStandaloneMacro(macroName, params));
	}
	
	@SuppressWarnings("boxing")
	public Rule NamedStandaloneMacro(StringVar macroName, StringVar params) {
		return Sequence(
				StandaloneMacroOpen(macroName, params),
				push(new MacroNode(macroName.get(), params.get())),
				"/}}"); //$NON-NLS-1$
	}
	
	public Rule StandaloneMacroOpen(StringVar macroName, StringVar params) {
		return Sequence(
				"{{", //$NON-NLS-1$
				MacroNameAndParameters(macroName, params, false));
	}

//	public Rule BodyMacro() {
//		StringVar macroName = new StringVar();
//		StringVar params = new StringVar();
//		return Sequence(
//				Test(BodyMacroOpen(macroName, params)),
//				NamedBodyMacro(macroName, params));
//	}
//
//	@SuppressWarnings("boxing")
//	public Rule NamedBodyMacro(StringVar macroName, StringVar params) {
//		return Sequence(
//				BodyMacroOpen(macroName, params),
//				push(new MacroNode(macroName.get(), params.get())),
//				// FIXME: does not work
//				ZeroOrMore(TestNot(BodyMacroClose()), Block(), addAsChild()),
//				BodyMacroClose());
//	}
//
//	public Rule BodyMacroOpen(StringVar macroName, StringVar params) {
//		return Sequence(
//				"{{", //$NON-NLS-1$
//				MacroNameAndParameters(macroName, params, true),
//				"}}"); //$NON-NLS-1$
//	}
//	
//	public Rule BodyMacroClose() {
//		return String("{{/}}"); //$NON-NLS-1$
//	}
	
	public Rule MacroNameAndParameters(StringVar macroName, StringVar params, boolean allowSlashInParams) {
		return Sequence(
				MacroName(macroName),
				Optional(
						Sequence(
								Spacechar(),
								MacroParameters(params, allowSlashInParams)
						)
				)
		);
	}

	@SuppressWarnings("boxing")
	public Rule MacroName(StringVar macroName) {
		return Sequence(
				OneOrMore(Alphanumeric()),
				macroName.isSet() && match().equals(macroName.get()) ||
					macroName.isNotSet() && macroName.set(match()));
	}
	
	@SuppressWarnings("boxing")
	public Rule MacroParameters(StringVar params, boolean allowSlash) {
		return Sequence(
				OneOrMore(
						TestNot(allowSlash ? Ch('}') : AnyOf("/}")), //$NON-NLS-1$
						ANY),
				params.isSet() && match().equals(params.get()) ||
					params.isNotSet() && params.set(match()));
	}

	@Override
	public Rule SpecialChar() {
		AnyOfMatcher anyOf = (AnyOfMatcher) super.SpecialChar();
		String chars = String.valueOf(anyOf.characters.getChars());
		chars += "{}/"; //$NON-NLS-1$
		return AnyOf(chars);
	}

	@Override
	@Cached
	@SuppressWarnings("boxing")
	public Rule LinkSource() {
		StringBuilderVar url = new StringBuilderVar();
		return FirstOf(
				Sequence('(', LinkSource(), ')'),
				Sequence('<', LinkSource(), '>'),
				Sequence('#', AnchorName(url)),
				Sequence(TestNot(AnyOf("#")), Url(url)), //$NON-NLS-1$
				push(StringUtils.EMPTY)
		);
	}
	
	@SuppressWarnings("boxing")
	public Rule AnchorName(StringBuilderVar url) {
		return Sequence(AnchorNameChars(url), push("#" + url.getString())); //$NON-NLS-1$
	}

	@SuppressWarnings("boxing")
	public Rule Url(StringBuilderVar url) {
		return Sequence(UrlChars(url), push(url.getString()));
	}
	
	@SuppressWarnings("boxing")
	public Rule AnchorNameChars(StringBuilderVar url) {
		return OneOrMore(
				Sequence(NoneOf("()<>[]#"), url.append(matchedChar())) //$NON-NLS-1$
		);
	}

	@SuppressWarnings("boxing")
	public Rule UrlChars(StringBuilderVar url) {
		return OneOrMore(
				FirstOf(
						Sequence('\\', AnyOf("()"), url.append(matchedChar())), //$NON-NLS-1$
						Sequence(TestNot(AnyOf("()>")), Nonspacechar(), url.append(matchedChar())) //$NON-NLS-1$
				)
		);
	}
}